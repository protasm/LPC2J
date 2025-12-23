package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationStage;
import io.github.protasm.lpc2j.pipeline.CompilationUnit;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTInherit;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMapNode;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.IdentifierResolution;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierCall;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeIdentifier;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import io.github.protasm.lpc2j.semantic.SemanticScope.ScopedSymbol;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                // Field shadowing: inherited field remains in scope, but emit a warning to highlight
                // the name collision.
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Field '" + field.symbol().name() + "' shadows inherited field",
                                field.line()));
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

        for (ASTMethod method : astObject.methods()) {
            SemanticScope methodScope = new SemanticScope(objectScope);

            if (method.parameters() != null) {
                for (ASTParameter parameter : method.parameters()) {
                    declareUnique(parameter.symbol(), methodScope, problems, "parameter");
                }
            }

            for (ASTLocal local : method.locals()) {
                declareUnique(local.symbol(), methodScope, problems, "local");
            }

            ensureImplicitReturn(method);
            resolveIdentifiers(astObject, method, methodScope, problems);
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

    private void resolveIdentifiers(
            ASTObject astObject,
            ASTMethod method,
            SemanticScope methodScope,
            List<CompilationProblem> problems) {
        if (method.body() == null)
            return;

        resolveStatement(astObject, method, methodScope, method.body(), problems);
    }

    private void resolveStatement(
            ASTObject astObject,
            ASTMethod method,
            SemanticScope methodScope,
            ASTStatement statement,
            List<CompilationProblem> problems) {
        if (statement == null)
            return;

        if (statement instanceof io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock block) {
            for (ASTStatement nested : block.statements())
                resolveStatement(astObject, method, methodScope, nested, problems);
            return;
        }

        if (statement instanceof io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression exprStmt) {
            resolveExpression(astObject, method, methodScope, exprStmt.expression(), problems);
            return;
        }

        if (statement instanceof io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse ifStmt) {
            resolveExpression(astObject, method, methodScope, ifStmt.condition(), problems);
            resolveStatement(astObject, method, methodScope, ifStmt.thenBranch(), problems);
            resolveStatement(astObject, method, methodScope, ifStmt.elseBranch(), problems);
            return;
        }

        if (statement instanceof ASTStmtReturn stmtReturn) {
            resolveExpression(astObject, method, methodScope, stmtReturn.returnValue(), problems);
        }
    }

    private void resolveExpression(
            ASTObject astObject,
            ASTMethod method,
            SemanticScope methodScope,
            ASTExpression expression,
            List<CompilationProblem> problems) {
        if (expression == null)
            return;

        if (expression instanceof ASTExprIdentifierAccess identifierAccess) {
            IdentifierResolution resolution = resolveValueTarget(method, methodScope, identifierAccess.name());
            if (resolution == null) {
                problems.add(new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Unresolved identifier '" + identifierAccess.name() + "'.",
                        identifierAccess.line()));
                return;
            }

            identifierAccess.setResolution(resolution);
            return;
        }

        if (expression instanceof ASTExprIdentifierStore store) {
            resolveExpression(astObject, method, methodScope, store.value(), problems);
            IdentifierResolution resolution = resolveValueTarget(method, methodScope, store.name());
            if (resolution == null) {
                problems.add(new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Unresolved assignment target '" + store.name() + "'.",
                        store.line()));
                return;
            }

            store.setResolution(resolution);
            return;
        }

        if (expression instanceof ASTExprIdentifierCall call) {
            resolveArguments(astObject, method, methodScope, call.arguments(), problems);
            IdentifierResolution resolution = resolveCallable(methodScope, call.name(), call.arguments().size());
            if (resolution == null) {
                problems.add(new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Unrecognized method or function '" + call.name() + "'.",
                        call.line()));
                return;
            }

            call.setResolution(resolution);
            return;
        }

        if (expression instanceof ASTExprInvokeIdentifier invoke) {
            resolveArguments(astObject, method, methodScope, invoke.arguments(), problems);
            ASTLocal local = findLocal(method, invoke.targetName());
            if (local == null) {
                problems.add(new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Unresolved local '" + invoke.targetName() + "' for invocation.",
                        invoke.line()));
                return;
            }

            invoke.setResolution(IdentifierResolution.forLocal(local));
            return;
        }

        if (expression instanceof io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpBinary binary) {
            resolveExpression(astObject, method, methodScope, binary.left(), problems);
            resolveExpression(astObject, method, methodScope, binary.right(), problems);
            return;
        }

        if (expression instanceof io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpUnary unary) {
            resolveExpression(astObject, method, methodScope, unary.right(), problems);
            return;
        }

        if (expression instanceof io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayAccess arrayAccess) {
            resolveExpression(astObject, method, methodScope, arrayAccess.target(), problems);
            resolveExpression(astObject, method, methodScope, arrayAccess.index(), problems);
            return;
        }

        if (expression instanceof io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayStore arrayStore) {
            resolveExpression(astObject, method, methodScope, arrayStore.target(), problems);
            resolveExpression(astObject, method, methodScope, arrayStore.index(), problems);
            resolveExpression(astObject, method, methodScope, arrayStore.value(), problems);
            return;
        }

        if (expression instanceof io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayLiteral arrayLiteral) {
            for (ASTExpression element : arrayLiteral.elements())
                resolveExpression(astObject, method, methodScope, element, problems);
            return;
        }

        if (expression instanceof io.github.protasm.lpc2j.parser.ast.expr.ASTExprMappingLiteral mappingLiteral) {
            for (io.github.protasm.lpc2j.parser.ast.expr.ASTExprMappingEntry entry : mappingLiteral.entries()) {
                resolveExpression(astObject, method, methodScope, entry.key(), problems);
                resolveExpression(astObject, method, methodScope, entry.value(), problems);
            }
            return;
        }

        if (expression instanceof io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod callMethod) {
            resolveArguments(astObject, method, methodScope, callMethod.arguments(), problems);
            return;
        }

        if (expression instanceof io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun callEfun) {
            resolveArguments(astObject, method, methodScope, callEfun.arguments(), problems);
            return;
        }

        if (expression instanceof io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeLocal invokeLocal) {
            resolveArguments(astObject, method, methodScope, invokeLocal.arguments(), problems);
        }
    }

    private void resolveArguments(
            ASTObject astObject,
            ASTMethod method,
            SemanticScope methodScope,
            io.github.protasm.lpc2j.parser.ast.ASTArguments arguments,
            List<CompilationProblem> problems) {
        if (arguments == null)
            return;

        for (io.github.protasm.lpc2j.parser.ast.ASTArgument argument : arguments)
            resolveExpression(astObject, method, methodScope, argument.expression(), problems);
    }

    private IdentifierResolution resolveValueTarget(
            ASTMethod method, SemanticScope methodScope, String name) {
        ASTLocal local = findLocal(method, name);
        if (local != null)
            return IdentifierResolution.forLocal(local);

        ASTField field = resolveField(methodScope, name);
        if (field != null)
            return IdentifierResolution.forField(field);

        return null;
    }

    private IdentifierResolution resolveCallable(SemanticScope methodScope, String name, int arity) {
        ASTMethod method = resolveMethod(methodScope, name);
        if (method != null)
            return IdentifierResolution.forMethod(method);

        io.github.protasm.lpc2j.efun.Efun efun = runtimeContext.resolveEfun(name, arity);
        if (efun != null)
            return IdentifierResolution.forEfun(efun);

        return null;
    }

    private ASTLocal findLocal(ASTMethod method, String name) {
        if (method.locals() == null)
            return null;

        for (ASTLocal local : method.locals()) {
            if (local.symbol().name().equals(name))
                return local;
        }

        return null;
    }

    private ASTField resolveField(SemanticScope scope, String name) {
        if (scope == null)
            return null;

        return scope.resolveAll(name).stream()
                .map(ScopedSymbol::field)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private ASTMethod resolveMethod(SemanticScope methodScope, String name) {
        SemanticScope scope = (methodScope != null) ? methodScope.parent() : null;
        if (scope == null)
            return null;

        return scope.resolveAll(name).stream()
                .map(ScopedSymbol::method)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
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

}
