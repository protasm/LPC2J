package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationStage;
import io.github.protasm.lpc2j.pipeline.CompilationUnit;
import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTInherit;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMapNode;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayLiteral;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeField;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprMappingEntry;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprMappingLiteral;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprNull;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprTernary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedAssignment;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedCall;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedInvoke;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedParentCall;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBreak;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtFor;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.AssignOpType;
import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;
import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import io.github.protasm.lpc2j.semantic.SemanticScope.ScopedSymbol;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/** Performs semantic analysis on a parsed AST and produces a typed model. */
public final class SemanticAnalyzer {
    private final TypeResolver typeResolver;
    private final RuntimeContext runtimeContext;

    public SemanticAnalyzer() {
        this(new TypeResolver(), null);
    }

    public SemanticAnalyzer(TypeResolver typeResolver) {
        this(typeResolver, null);
    }

    public SemanticAnalyzer(RuntimeContext runtimeContext) {
        this(new TypeResolver(), runtimeContext);
    }

    public SemanticAnalyzer(TypeResolver typeResolver, RuntimeContext runtimeContext) {
        this.typeResolver = Objects.requireNonNull(typeResolver, "typeResolver");
        this.runtimeContext =
                (runtimeContext != null) ? runtimeContext : new RuntimeContext(null);
    }

    public SemanticAnalysisResult analyze(ASTObject astObject) {
        return analyze(astObject, null);
    }

    public SemanticAnalysisResult analyze(CompilationUnit unit) {
        if (unit == null)
            throw new IllegalArgumentException("CompilationUnit cannot be null.");

        return analyze(unit.astObject(), unit);
    }

    private SemanticAnalysisResult analyze(ASTObject astObject, CompilationUnit unit) {
        if (astObject == null)
            throw new IllegalArgumentException("ASTObject cannot be null.");

        List<CompilationProblem> problems = new ArrayList<>();
        CompilationUnit parentUnit = (unit != null) ? unit.parentUnit() : null;
        SemanticScope parentScope = (parentUnit != null && parentUnit.semanticModel() != null)
                ? parentUnit.semanticModel().objectScope()
                : null;
        SemanticScope objectScope = new SemanticScope(parentScope);

        validateInheritance(astObject, problems);
        resolveObjectSignatures(astObject, problems);
        validateDefinitionsHaveDeclarations(astObject, problems);
        validateDuplicates(astObject.fields(), "field", problems);
        validateDuplicates(astObject.methods(), "method", problems);
        mergeParentSymbols(objectScope, parentUnit);

        for (ASTField field : astObject.fields()) {
            boolean shadowsInherited =
                    hasInheritedField(parentScope, field.symbol().name());
            if (shadowsInherited) {
                // Field shadowing: inherited field remains in scope. Shadowing is permitted for
                // compatibility with LPC mudlibs, so we do not surface it as an error.
            }

            objectScope.declare(field.symbol(), unit, field, null);
        }

        for (ASTMethod method : astObject.methods()) {
            ASTMethod overridden = findOverriddenMethod(parentScope, method);
            if (overridden != null && !isSignatureCompatible(method, overridden)) {
                // Override detection: overriding is allowed only when the typed LPC signatures
                // match; otherwise surface a hard error.
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Method '" + method.symbol().name() + "' overrides with incompatible signature",
                                method.line()));
            }
            method.setOverrides(overridden);

            objectScope.declare(method.symbol(), unit, null, method);
        }

        resolveIdentifiers(astObject, objectScope, parentUnit, problems);

        for (ASTMethod method : astObject.methods()) {
            SemanticScope methodScope = new SemanticScope(objectScope);

            declareParameters(methodScope, method, problems);
            assignLocalSlots(method, problems);
            validateLocalInitializers(method, problems);
            ensureImplicitReturn(method);
            resolveLocalTypes(method, problems);
        }

        SemanticTypeChecker typeChecker = new SemanticTypeChecker(problems);
        typeChecker.check(astObject);

        return new SemanticAnalysisResult(new SemanticModel(astObject, objectScope), problems);
    }

    private void resolveSymbolType(Symbol symbol, int line, List<CompilationProblem> problems) {
        if (symbol == null)
            return;

        if (symbol.lpcType() != null)
            return;

        LPCType resolved = typeResolver.resolve(symbol.declaredTypeName());

        if (resolved == null) {
            if (symbol.declaredTypeName() != null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Unknown type '" + symbol.declaredTypeName() + "' for symbol '" + symbol.name() + "'",
                                line));
            }

            symbol.resolveDeclaredType(LPCType.LPCMIXED);
            return;
        }

        symbol.resolveDeclaredType(resolved);
    }

    private void declareUnique(
            Symbol symbol, SemanticScope scope, List<CompilationProblem> problems, String kind) {
        ScopedSymbol existing = scope.resolveLocally(symbol.name());

        if (existing != null && existing.symbol() != symbol) {
            // Duplicate detection is limited to the child scope; inherited entries have already
            // been merged with origin metadata, so seeing a different symbol here means the child
            // redeclared the same name.
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Duplicate " + kind + " '" + symbol.name() + "' in scope",
                            (Throwable) null));
            return;
        }

        scope.declare(symbol);
    }

    private void declareParameters(
            SemanticScope methodScope, ASTMethod method, List<CompilationProblem> problems) {
        if (method.parameters() == null)
            return;

        for (ASTParameter parameter : method.parameters())
            declareUnique(parameter.symbol(), methodScope, problems, "parameter");
    }

    private int parameterCount(ASTMethod method) {
        return (method.parameters() != null) ? method.parameters().size() : 0;
    }

    private void resolveLocalTypes(ASTMethod method, List<CompilationProblem> problems) {
        if (method.locals() == null)
            return;

        for (ASTLocal local : method.locals())
            resolveSymbolType(local.symbol(), local.line(), problems);
    }

    private void ensureImplicitReturn(ASTMethod method) {
        if (method.body() == null)
            return;

        List<ASTStatement> statements = method.body().statements();

        if (statements.isEmpty() || !(statements.get(statements.size() - 1) instanceof ASTStmtReturn))
            statements.add(new ASTStmtReturn(method.body().line(), defaultReturnValue(method), true));
    }

    private ASTExpression defaultReturnValue(ASTMethod method) {
        LPCType methodType = method.symbol().lpcType();
        if (methodType == LPCType.LPCVOID)
            return null;

        return new ASTExprLiteralInteger(
                method.body().line(), new Token<>(TokenType.T_INT_LITERAL, "0", 0, null));
    }

    private void assignLocalSlots(ASTMethod method, List<CompilationProblem> problems) {
        Set<Symbol> parameterSymbols = parameterSymbols(method);
        int slot = 1; // slot 0 reserved for "this"

        if (method.parameters() != null) {
            for (ASTParameter parameter : method.parameters()) {
                ASTLocal paramLocal = findLocalForSymbol(method.locals(), parameter.symbol());
                if (paramLocal != null) {
                    paramLocal.setSlot(slot);
                    paramLocal.setScopeDepth(Math.max(paramLocal.scopeDepth(), 0));
                }
                slot++;
            }
        }

        LocalSlotAllocator allocator = new LocalSlotAllocator(parameterCount(method));

        for (ASTLocal local : method.locals()) {
            if (parameterSymbols.contains(local.symbol()))
                continue;
            allocator.place(local, problems);
        }
    }

    private Set<Symbol> parameterSymbols(ASTMethod method) {
        if (method.parameters() == null)
            return Set.of();

        Set<Symbol> symbols = new HashSet<>();
        for (ASTParameter parameter : method.parameters())
            symbols.add(parameter.symbol());
        return symbols;
    }

    private ASTLocal findLocalForSymbol(List<ASTLocal> locals, Symbol symbol) {
        if (locals == null || symbol == null)
            return null;

        for (ASTLocal local : locals) {
            if (local.symbol() == symbol)
                return local;
        }

        return null;
    }

    private void validateLocalInitializers(ASTMethod method, List<CompilationProblem> problems) {
        if (method.body() == null)
            return;

        validateInitializers(method.body(), problems);
    }

    private void validateInitializers(ASTStatement statement, List<CompilationProblem> problems) {
        if (statement == null)
            return;

        if (statement instanceof ASTStmtBlock block) {
            for (ASTStatement nested : block)
                validateInitializers(nested, problems);
            return;
        }

        if (statement instanceof ASTStmtIfThenElse stmtIf) {
            validateInitializers(stmtIf.thenBranch(), problems);
            if (stmtIf.elseBranch() != null)
                validateInitializers(stmtIf.elseBranch(), problems);
            return;
        }

        if (statement instanceof ASTStmtFor stmtFor) {
            inspectInitializerExpression(stmtFor.initializer(), problems);
            inspectInitializerExpression(stmtFor.update(), problems);
            validateInitializers(stmtFor.body(), problems);
            return;
        }

        if (statement instanceof ASTStmtExpression stmtExpression)
            inspectInitializerExpression(stmtExpression.expression(), problems);
    }

    private void inspectInitializerExpression(ASTExpression expression, List<CompilationProblem> problems) {
        if (expression instanceof ASTExprLocalStore store && store.isDeclarationInitializer()) {
            if (referencesLocal(store.value(), store.local())) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Cannot reference local '" + store.local().symbol().name()
                                        + "' in its own initializer.",
                                store.line()));
            }
        }
    }

    private boolean referencesLocal(ASTExpression expression, ASTLocal local) {
        if (expression == null || local == null)
            return false;

        if (expression instanceof ASTExprLocalAccess access)
            return access.local() == local;

        if (expression instanceof ASTExprLocalStore store)
            return store.local() == local || referencesLocal(store.value(), local);

        if (expression instanceof ASTExprInvokeLocal invoke)
            return invoke.local() == local || referencesArguments(invoke.arguments(), local);

        if (expression instanceof ASTExprInvokeField invoke)
            return referencesArguments(invoke.arguments(), local);

        if (expression instanceof ASTExprCallMethod call)
            return referencesArguments(call.arguments(), local);

        if (expression instanceof ASTExprCallEfun call)
            return referencesArguments(call.arguments(), local);

        if (expression instanceof ASTExprArrayStore store)
            return referencesLocal(store.target(), local)
                    || referencesLocal(store.index(), local)
                    || referencesLocal(store.value(), local);

        if (expression instanceof ASTExprArrayAccess access)
            return referencesLocal(access.target(), local) || referencesLocal(access.index(), local);

        if (expression instanceof ASTExprArrayLiteral arrayLiteral)
            return arrayLiteral.elements().stream().anyMatch(elem -> referencesLocal(elem, local));

        if (expression instanceof ASTExprMappingLiteral mappingLiteral)
            return mappingLiteral.entries().stream()
                    .anyMatch(entry -> referencesLocal(entry.key(), local) || referencesLocal(entry.value(), local));

        if (expression instanceof ASTExprOpUnary unary)
            return referencesLocal(unary.right(), local);

        if (expression instanceof ASTExprOpBinary binary)
            return referencesLocal(binary.left(), local) || referencesLocal(binary.right(), local);

        if (expression instanceof ASTExprTernary ternary) {
            return referencesLocal(ternary.condition(), local)
                    || referencesLocal(ternary.thenBranch(), local)
                    || referencesLocal(ternary.elseBranch(), local);
        }

        return false;
    }

    private boolean referencesArguments(ASTArguments arguments, ASTLocal local) {
        if (arguments == null)
            return false;

        for (ASTArgument argument : arguments)
            if (referencesLocal(argument.expression(), local))
                return true;

        return false;
    }

    private void validateInheritance(ASTObject astObject, List<CompilationProblem> problems) {
        List<ASTInherit> inherits = astObject.inherits();
        if (inherits.isEmpty())
            return;

        if (inherits.size() > 1) {
            for (int i = 1; i < inherits.size(); i++) {
                ASTInherit inherit = inherits.get(i);
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Only one inherit statement is allowed per object.",
                                inherit.line()));
            }
        }

        int firstPropertyLine = firstPropertyLine(astObject);
        if (firstPropertyLine == Integer.MAX_VALUE)
            return;

        for (ASTInherit inherit : inherits) {
            if (inherit.line() > firstPropertyLine) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "inherit statements must appear before any variable or function declarations.",
                                inherit.line()));
            }
        }
    }

    private int firstPropertyLine(ASTObject astObject) {
        int firstLine = Integer.MAX_VALUE;

        for (ASTField field : astObject.fields())
            firstLine = Math.min(firstLine, field.line());

        for (ASTMethod method : astObject.methods())
            firstLine = Math.min(firstLine, method.line());

        return firstLine;
    }

    private void resolveObjectSignatures(ASTObject astObject, List<CompilationProblem> problems) {
        for (ASTField field : astObject.fields())
            resolveSymbolType(field.symbol(), field.line(), problems);

        for (ASTMethod method : astObject.methods()) {
            resolveSymbolType(method.symbol(), method.line(), problems);
            if (method.parameters() != null) {
                for (ASTParameter parameter : method.parameters())
                    resolveSymbolType(parameter.symbol(), parameter.line(), problems);
            }
        }
    }

    private void validateDefinitionsHaveDeclarations(
            ASTObject astObject, List<CompilationProblem> problems) {
        for (ASTField field : astObject.fields()) {
            if (field.isDefined() && !field.isDeclared()) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Field '" + field.symbol().name() + "' is defined without a prior declaration.",
                                field.line()));
            }
        }

        for (ASTMethod method : astObject.methods()) {
            if (method.isDefined() && !method.isDeclared()) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Method '" + method.symbol().name() + "' is defined without a prior declaration.",
                                method.line()));
            }
        }
    }

    private <T> void validateDuplicates(ASTMapNode<T> nodes, String kind, List<CompilationProblem> problems) {
        for (Map.Entry<String, List<T>> entry : nodes.nodes().entrySet()) {
            List<T> occurrences = entry.getValue();
            if (occurrences.size() > 1) {
                // Emit once per duplicate cluster to surface user-facing name collisions.
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Duplicate " + kind + " '" + entry.getKey() + "' in object",
                                nodes.line()));
            }
        }
    }

    private void mergeParentSymbols(SemanticScope objectScope, CompilationUnit parentUnit) {
        if (parentUnit == null || parentUnit.semanticModel() == null)
            return;

        ASTObject parent = parentUnit.semanticModel().astObject();
        for (ASTField field : parent.fields())
            objectScope.importSymbol(new ScopedSymbol(field.symbol(), parentUnit, field, null));
        for (ASTMethod method : parent.methods())
            objectScope.importSymbol(new ScopedSymbol(method.symbol(), parentUnit, null, method));
    }

    private boolean hasInheritedField(SemanticScope parentScope, String name) {
        if (parentScope == null)
            return false;

        return parentScope.resolveAll(name).stream().anyMatch(s -> s.field() != null);
    }

    private ASTMethod findOverriddenMethod(SemanticScope parentScope, ASTMethod method) {
        if (parentScope == null)
            return null;

        return parentScope.resolveAll(method.symbol().name()).stream()
                .map(ScopedSymbol::method)
                .filter(Objects::nonNull)
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private boolean isSignatureCompatible(ASTMethod child, ASTMethod parent) {
        if (child == null || parent == null)
            return false;

        LPCType childReturn = child.symbol().lpcType();
        LPCType parentReturn = parent.symbol().lpcType();
        if (childReturn != null && parentReturn != null && childReturn != parentReturn)
            return false;

        List<LPCType> childParams = parameterTypes(child);
        List<LPCType> parentParams = parameterTypes(parent);

        if (childParams.size() != parentParams.size())
            return false;

        for (int i = 0; i < childParams.size(); i++) {
            LPCType childType = childParams.get(i);
            LPCType parentType = parentParams.get(i);
            if (childType != null && parentType != null && childType != parentType)
                return false;
        }

        return true;
    }

    private List<LPCType> parameterTypes(ASTMethod method) {
        if (method.parameters() == null)
            return List.of();

        List<LPCType> types = new ArrayList<>(method.parameters().size());
        method.parameters().forEach(param -> types.add(param.symbol().lpcType()));
        return types;
    }

    private void resolveIdentifiers(
            ASTObject astObject, SemanticScope objectScope, CompilationUnit parentUnit, List<CompilationProblem> problems) {
        IdentifierResolver resolver = new IdentifierResolver(objectScope, parentUnit, runtimeContext, problems);

        for (ASTField field : astObject.fields()) {
            if (field.initializer() != null)
                field.setInitializer(resolver.resolveExpression(field.initializer(), null));
        }

        for (ASTMethod method : astObject.methods())
            resolver.resolveMethod(method);
    }

    private static final class LocalResolutionContext {
        private final Deque<List<ASTLocal>> scopes = new ArrayDeque<>();

        void pushScope() {
            scopes.push(new ArrayList<>());
        }

        void popScope() {
            scopes.pop();
        }

        void declare(List<ASTLocal> locals) {
            if (locals == null || locals.isEmpty())
                return;

            scopes.peek().addAll(locals);
        }

        ASTLocal resolve(String name) {
            for (List<ASTLocal> scope : scopes) {
                for (int i = scope.size() - 1; i >= 0; i--) {
                    ASTLocal local = scope.get(i);
                    if (local.symbol().name().equals(name))
                        return local;
                }
            }

            return null;
        }
    }

    private static final class IdentifierResolver {
        private final SemanticScope objectScope;
        private final CompilationUnit parentUnit;
        private final RuntimeContext runtimeContext;
        private final List<CompilationProblem> problems;

        IdentifierResolver(
                SemanticScope objectScope,
                CompilationUnit parentUnit,
                RuntimeContext runtimeContext,
                List<CompilationProblem> problems) {
            this.objectScope = objectScope;
            this.parentUnit = parentUnit;
            this.runtimeContext = runtimeContext;
            this.problems = problems;
        }

        void resolveMethod(ASTMethod method) {
            if (method.body() == null)
                return;

            LocalResolutionContext context = new LocalResolutionContext();
            context.pushScope();
            context.declare(localsAtDepth(method.locals(), 0));
            method.setBody(resolveBlock(method.body(), context));
            context.popScope();
        }

        private List<ASTLocal> localsAtDepth(List<ASTLocal> locals, int depth) {
            if (locals == null || locals.isEmpty())
                return List.of();

            List<ASTLocal> scoped = new ArrayList<>();
            for (ASTLocal local : locals) {
                if (local.scopeDepth() == depth)
                    scoped.add(local);
            }
            return scoped;
        }

        ASTExpression resolveExpression(ASTExpression expression, LocalResolutionContext context) {
            if (expression == null)
                return null;

            if (expression instanceof ASTExprUnresolvedIdentifier unresolvedIdentifier)
                return resolveIdentifier(unresolvedIdentifier, context);

            if (expression instanceof ASTExprUnresolvedAssignment unresolvedAssignment)
                return resolveAssignment(unresolvedAssignment, context);

            if (expression instanceof ASTExprUnresolvedCall unresolvedCall)
                return resolveCall(unresolvedCall, context);

            if (expression instanceof ASTExprUnresolvedParentCall unresolvedParentCall)
                return resolveParentCall(unresolvedParentCall, context);

            if (expression instanceof ASTExprUnresolvedInvoke unresolvedInvoke)
                return resolveInvoke(unresolvedInvoke, context);

            if (expression instanceof ASTExprLocalStore store) {
                ASTExpression resolvedValue = resolveExpression(store.value(), context);
                if (resolvedValue == store.value())
                    return store;
                return new ASTExprLocalStore(store.line(), store.local(), resolvedValue, store.isDeclarationInitializer());
            }

            if (expression instanceof ASTExprFieldStore store) {
                ASTExpression resolvedValue = resolveExpression(store.value(), context);
                if (resolvedValue == store.value())
                    return store;
                return new ASTExprFieldStore(store.line(), store.field(), resolvedValue);
            }

            if (expression instanceof ASTExprArrayStore store) {
                ASTExpression resolvedTarget = resolveExpression(store.target(), context);
                ASTExpression resolvedIndex = resolveExpression(store.index(), context);
                ASTExpression resolvedValue = resolveExpression(store.value(), context);

                if (resolvedTarget == store.target()
                        && resolvedIndex == store.index()
                        && resolvedValue == store.value())
                    return store;

                return new ASTExprArrayStore(store.line(), resolvedTarget, resolvedIndex, resolvedValue);
            }

            if (expression instanceof ASTExprArrayAccess access) {
                ASTExpression resolvedTarget = resolveExpression(access.target(), context);
                ASTExpression resolvedIndex = resolveExpression(access.index(), context);
                if (resolvedTarget == access.target() && resolvedIndex == access.index())
                    return access;
                return new ASTExprArrayAccess(access.line(), resolvedTarget, resolvedIndex);
            }

            if (expression instanceof ASTExprLocalAccess access) {
                return access;
            }

            if (expression instanceof ASTExprFieldAccess access) {
                return access;
            }

            if (expression instanceof ASTExprOpUnary unary) {
                ASTExpression resolvedRight = resolveExpression(unary.right(), context);
                if (resolvedRight == unary.right())
                    return unary;
                return new ASTExprOpUnary(unary.line(), resolvedRight, unary.operator());
            }

            if (expression instanceof ASTExprOpBinary binary) {
                ASTExpression resolvedLeft = resolveExpression(binary.left(), context);
                ASTExpression resolvedRight = resolveExpression(binary.right(), context);
                if (resolvedLeft == binary.left() && resolvedRight == binary.right())
                    return binary;
                return new ASTExprOpBinary(binary.line(), resolvedLeft, resolvedRight, binary.operator());
            }

            if (expression instanceof ASTExprTernary ternary) {
                ASTExpression resolvedCondition = resolveExpression(ternary.condition(), context);
                ASTExpression resolvedThen = resolveExpression(ternary.thenBranch(), context);
                ASTExpression resolvedElse = resolveExpression(ternary.elseBranch(), context);
                if (resolvedCondition == ternary.condition()
                        && resolvedThen == ternary.thenBranch()
                        && resolvedElse == ternary.elseBranch())
                    return ternary;
                return new ASTExprTernary(ternary.line(), resolvedCondition, resolvedThen, resolvedElse);
            }

            if (expression instanceof ASTExprCallMethod callMethod) {
                ASTArguments resolvedArgs = resolveArguments(callMethod.arguments(), context);
                if (resolvedArgs == callMethod.arguments())
                    return callMethod;
                return new ASTExprCallMethod(
                        callMethod.line(), callMethod.method(), resolvedArgs, callMethod.isParentDispatch());
            }

            if (expression instanceof ASTExprCallEfun callEfun) {
                ASTArguments resolvedArgs = resolveArguments(callEfun.arguments(), context);
                if (resolvedArgs == callEfun.arguments())
                    return callEfun;
                return new ASTExprCallEfun(callEfun.line(), callEfun.efun(), resolvedArgs);
            }

            if (expression instanceof ASTExprInvokeLocal invokeLocal) {
                ASTArguments resolvedArgs = resolveArguments(invokeLocal.arguments(), context);
                if (resolvedArgs == invokeLocal.arguments())
                    return invokeLocal;
                return new ASTExprInvokeLocal(invokeLocal.line(), invokeLocal.local(), invokeLocal.methodName(), resolvedArgs);
            }

            if (expression instanceof ASTExprInvokeField invokeField) {
                ASTArguments resolvedArgs = resolveArguments(invokeField.arguments(), context);
                if (resolvedArgs == invokeField.arguments())
                    return invokeField;
                return new ASTExprInvokeField(
                        invokeField.line(), invokeField.field(), invokeField.methodName(), resolvedArgs);
            }

            if (expression instanceof ASTExprArrayLiteral arrayLiteral) {
                List<ASTExpression> resolvedElements = new ArrayList<>();
                boolean changed = false;
                for (ASTExpression element : arrayLiteral.elements()) {
                    ASTExpression resolved = resolveExpression(element, context);
                    changed |= resolved != element;
                    resolvedElements.add(resolved);
                }
                if (!changed)
                    return arrayLiteral;
                return new ASTExprArrayLiteral(arrayLiteral.line(), resolvedElements);
            }

            if (expression instanceof ASTExprMappingLiteral mappingLiteral) {
                List<ASTExprMappingEntry> resolvedEntries = new ArrayList<>();
                boolean changed = false;
                for (ASTExprMappingEntry entry : mappingLiteral.entries()) {
                    ASTExpression resolvedKey = resolveExpression(entry.key(), context);
                    ASTExpression resolvedValue = resolveExpression(entry.value(), context);
                    changed |= resolvedKey != entry.key() || resolvedValue != entry.value();
                    resolvedEntries.add(new ASTExprMappingEntry(resolvedKey, resolvedValue));
                }
                if (!changed)
                    return mappingLiteral;
                return new ASTExprMappingLiteral(mappingLiteral.line(), resolvedEntries);
            }

            return expression;
        }

        private ASTExpression resolveInvoke(ASTExprUnresolvedInvoke unresolvedInvoke, LocalResolutionContext context) {
            ASTArguments resolvedArgs = resolveArguments(unresolvedInvoke.arguments(), context);
            ASTLocal local = resolveLocal(context, unresolvedInvoke.targetName());

            if (local != null)
                return new ASTExprInvokeLocal(unresolvedInvoke.line(), local, unresolvedInvoke.methodName(), resolvedArgs);

            ScopedSymbol scopedSymbol = resolveScopedSymbol(unresolvedInvoke.targetName());
            if (scopedSymbol != null && scopedSymbol.field() != null) {
                return new ASTExprInvokeField(
                        unresolvedInvoke.line(), scopedSymbol.field(), unresolvedInvoke.methodName(), resolvedArgs);
            }

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unrecognized invoke target '" + unresolvedInvoke.targetName() + "'",
                            unresolvedInvoke.line()));
            return new ASTExprNull(unresolvedInvoke.line());
        }

        private ASTExpression resolveCall(ASTExprUnresolvedCall unresolvedCall, LocalResolutionContext context) {
            ASTArguments resolvedArgs = resolveArguments(unresolvedCall.arguments(), context);
            ASTMethod method = resolveMethod(unresolvedCall.name());

            if (method != null)
                return new ASTExprCallMethod(unresolvedCall.line(), method, resolvedArgs);

            Efun efun = runtimeContext.resolveEfun(unresolvedCall.name(), resolvedArgs.size());

            if (efun != null)
                return new ASTExprCallEfun(unresolvedCall.line(), efun, resolvedArgs);

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unrecognized method or function '" + unresolvedCall.name() + "'.",
                            unresolvedCall.line()));
            return new ASTExprNull(unresolvedCall.line());
        }

        private ASTExpression resolveParentCall(
                ASTExprUnresolvedParentCall unresolvedParentCall, LocalResolutionContext context) {
            ASTArguments resolvedArgs = resolveArguments(unresolvedParentCall.arguments(), context);
            SemanticScope parentScope = (objectScope != null) ? objectScope.parent() : null;

            if (parentScope == null || parentUnit == null || parentUnit.semanticModel() == null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Cannot call inherited method '" + unresolvedParentCall.name() + "' without a parent object.",
                                unresolvedParentCall.line()));
                return new ASTExprNull(unresolvedParentCall.line());
            }

            ScopedSymbol parentSymbol = parentScope.resolveLocally(unresolvedParentCall.name());
            ASTMethod parentMethod = (parentSymbol != null) ? parentSymbol.method() : null;
            if (parentMethod != null)
                return new ASTExprCallMethod(unresolvedParentCall.line(), parentMethod, resolvedArgs, true);

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Inherited method '" + unresolvedParentCall.name() + "' is not defined in the parent object.",
                            unresolvedParentCall.line()));
            return new ASTExprNull(unresolvedParentCall.line());
        }

        private ASTExpression resolveAssignment(
                ASTExprUnresolvedAssignment unresolvedAssignment, LocalResolutionContext context) {
            ASTExpression resolvedValue = resolveExpression(unresolvedAssignment.value(), context);
            ASTLocal local = resolveLocal(context, unresolvedAssignment.name());

            if (local != null)
                return buildLocalStore(unresolvedAssignment, local, resolvedValue);

            ScopedSymbol scopedSymbol = resolveScopedSymbol(unresolvedAssignment.name());
            if (scopedSymbol != null && scopedSymbol.field() != null)
                return buildFieldStore(unresolvedAssignment, scopedSymbol.field(), resolvedValue);

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unrecognized local or field '" + unresolvedAssignment.name() + "'.",
                            unresolvedAssignment.line()));
            return new ASTExprNull(unresolvedAssignment.line());
        }

        private ASTExpression resolveIdentifier(
                ASTExprUnresolvedIdentifier unresolvedIdentifier, LocalResolutionContext context) {
            ASTLocal local = resolveLocal(context, unresolvedIdentifier.name());
            if (local != null)
                return new ASTExprLocalAccess(unresolvedIdentifier.line(), local);

            ScopedSymbol scopedSymbol = resolveScopedSymbol(unresolvedIdentifier.name());
            if (scopedSymbol != null && scopedSymbol.field() != null)
                return new ASTExprFieldAccess(unresolvedIdentifier.line(), scopedSymbol.field());

            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unrecognized local or field '" + unresolvedIdentifier.name() + "'.",
                            unresolvedIdentifier.line()));
            return new ASTExprNull(unresolvedIdentifier.line());
        }

        private ASTExprFieldStore buildFieldStore(
                ASTExprUnresolvedAssignment assignment, ASTField field, ASTExpression resolvedValue) {
            ASTExpression value = resolvedValue;
            if (assignment.operator() == AssignOpType.ADD)
                value = new ASTExprOpBinary(assignment.line(), new ASTExprFieldAccess(assignment.line(), field),
                        resolvedValue, BinaryOpType.BOP_ADD);
            else if (assignment.operator() == AssignOpType.SUB)
                value = new ASTExprOpBinary(assignment.line(), new ASTExprFieldAccess(assignment.line(), field),
                        resolvedValue, BinaryOpType.BOP_SUB);

            return new ASTExprFieldStore(assignment.line(), field, value);
        }

        private ASTExprLocalStore buildLocalStore(
                ASTExprUnresolvedAssignment assignment, ASTLocal local, ASTExpression resolvedValue) {
            ASTExpression value = resolvedValue;
            if (assignment.operator() == AssignOpType.ADD)
                value = new ASTExprOpBinary(assignment.line(), new ASTExprLocalAccess(assignment.line(), local),
                        resolvedValue, BinaryOpType.BOP_ADD);
            else if (assignment.operator() == AssignOpType.SUB)
                value = new ASTExprOpBinary(assignment.line(), new ASTExprLocalAccess(assignment.line(), local),
                        resolvedValue, BinaryOpType.BOP_SUB);

            return new ASTExprLocalStore(assignment.line(), local, value);
        }

        private ASTMethod resolveMethod(String name) {
            ScopedSymbol scopedSymbol = resolveScopedSymbol(name);
            if (scopedSymbol == null)
                return null;

            return scopedSymbol.method();
        }

        private ScopedSymbol resolveScopedSymbol(String name) {
            if (objectScope == null)
                return null;

            return objectScope.resolve(name);
        }

        private ASTLocal resolveLocal(LocalResolutionContext context, String name) {
            if (context == null)
                return null;

            return context.resolve(name);
        }

        private ASTArguments resolveArguments(ASTArguments arguments, LocalResolutionContext context) {
            if (arguments == null)
                return null;

            ASTArguments resolvedArgs = new ASTArguments(arguments.line());
            boolean changed = false;
            for (ASTArgument argument : arguments) {
                ASTExpression resolvedExpr = resolveExpression(argument.expression(), context);
                changed |= resolvedExpr != argument.expression();
                resolvedArgs.add(new ASTArgument(argument.line(), resolvedExpr));
            }

            return changed ? resolvedArgs : arguments;
        }

        private ASTStmtBlock resolveBlock(ASTStmtBlock block, LocalResolutionContext context) {
            if (block == null)
                return null;

            context.pushScope();
            List<ASTStatement> resolvedStatements = new ArrayList<>(block.statements().size());
            List<ASTStmtBlock.BlockLocalDeclaration> localDeclarations = block.localDeclarations();
            int declarationCursor = 0;

            for (int i = 0; i <= block.statements().size(); i++) {
                while (declarationCursor < localDeclarations.size()
                        && localDeclarations.get(declarationCursor).statementIndex() == i) {
                    context.declare(localDeclarations.get(declarationCursor).locals());
                    declarationCursor++;
                }

                if (i == block.statements().size())
                    break;

                resolvedStatements.add(resolveStatement(block.statements().get(i), context));
            }

            context.popScope();
            return new ASTStmtBlock(block.line(), resolvedStatements, localDeclarations);
        }

        private ASTStatement resolveStatement(ASTStatement statement, LocalResolutionContext context) {
            if (statement == null)
                return null;

            if (statement instanceof ASTStmtBlock block)
                return resolveBlock(block, context);

            if (statement instanceof ASTStmtExpression stmtExpression) {
                ASTExpression resolved = resolveExpression(stmtExpression.expression(), context);
                if (resolved == stmtExpression.expression())
                    return stmtExpression;
                return new ASTStmtExpression(stmtExpression.line(), resolved);
            }

            if (statement instanceof ASTStmtIfThenElse stmtIf) {
                ASTExpression resolvedCondition = resolveExpression(stmtIf.condition(), context);
                ASTStatement resolvedThen = resolveStatement(stmtIf.thenBranch(), context);
                ASTStatement resolvedElse = resolveStatement(stmtIf.elseBranch(), context);
                if (resolvedCondition == stmtIf.condition()
                        && resolvedThen == stmtIf.thenBranch()
                        && resolvedElse == stmtIf.elseBranch())
                    return stmtIf;
                return new ASTStmtIfThenElse(stmtIf.line(), resolvedCondition, resolvedThen, resolvedElse);
            }

            if (statement instanceof ASTStmtFor stmtFor) {
                ASTExpression resolvedInit = resolveExpression(stmtFor.initializer(), context);
                ASTExpression resolvedCondition = resolveExpression(stmtFor.condition(), context);
                ASTExpression resolvedUpdate = resolveExpression(stmtFor.update(), context);
                ASTStatement resolvedBody = resolveStatement(stmtFor.body(), context);
                if (resolvedInit == stmtFor.initializer()
                        && resolvedCondition == stmtFor.condition()
                        && resolvedUpdate == stmtFor.update()
                        && resolvedBody == stmtFor.body())
                    return stmtFor;
                return new ASTStmtFor(stmtFor.line(), resolvedInit, resolvedCondition, resolvedUpdate, resolvedBody);
            }

            if (statement instanceof ASTStmtBreak stmtBreak)
                return stmtBreak;

            if (statement instanceof ASTStmtReturn stmtReturn) {
                ASTExpression resolved = resolveExpression(stmtReturn.returnValue(), context);
                if (resolved == stmtReturn.returnValue())
                    return stmtReturn;
                return new ASTStmtReturn(stmtReturn.line(), resolved);
            }

            return statement;
        }
    }

    private static final class LocalSlotAllocator {
        private final Deque<ScopeFrame> scopes = new ArrayDeque<>();
        private final Deque<Integer> freeSlots = new ArrayDeque<>();
        private int currentDepth = 0;
        private int nextSlot;

        LocalSlotAllocator(int parameterCount) {
            nextSlot = parameterCount + 1; // slot 0 reserved for "this"
            scopes.push(new ScopeFrame());
        }

        void place(ASTLocal local, List<CompilationProblem> problems) {
            int targetDepth = Math.max(local.scopeDepth(), 0);
            alignScopes(targetDepth);

            ScopeFrame frame = scopes.peek();
            ASTLocal existing = frame.locals.get(local.symbol().name());
            if (existing != null && existing != local) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Duplicate local '" + local.symbol().name() + "' in scope",
                                local.line()));
            }

            int slot = (!freeSlots.isEmpty()) ? freeSlots.pop() : nextSlot++;
            local.setSlot(slot);
            local.setScopeDepth(targetDepth);
            frame.locals.put(local.symbol().name(), local);
        }

        private void alignScopes(int targetDepth) {
            while (currentDepth > targetDepth)
                releaseScope();

            while (currentDepth < targetDepth)
                openScope();
        }

        private void openScope() {
            scopes.push(new ScopeFrame());
            currentDepth++;
        }

        private void releaseScope() {
            ScopeFrame expired = scopes.pop();
            for (ASTLocal local : expired.locals.values()) {
                if (local.slot() >= 0)
                    freeSlots.push(local.slot());
            }
            currentDepth--;
        }

        private static final class ScopeFrame {
            private final Map<String, ASTLocal> locals = new HashMap<>();
        }
    }

}
