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
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
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
