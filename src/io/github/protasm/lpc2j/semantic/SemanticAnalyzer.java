package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationStage;
import io.github.protasm.lpc2j.pipeline.CompilationUnit;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenType;

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

    public SemanticAnalysisResult analyze(ASTObject astObject, CompilationUnit compilationUnit) {
        if (astObject == null)
            throw new IllegalArgumentException("ASTObject cannot be null.");

        List<CompilationProblem> problems = new ArrayList<>();
        SemanticScope objectScope = new SemanticScope();

        CompilationUnit parentUnit = (compilationUnit != null) ? compilationUnit.parentUnit() : null;
        SemanticModel parentModel = (parentUnit != null) ? parentUnit.semanticModel() : null;

        // Inherit the parent's symbols into the child's object scope so that inherited members are
        // visible without changing runtime behavior yet.
        importParentSymbols(objectScope, parentUnit, parentModel);

        for (ASTField field : astObject.fields())
            declareField(field, objectScope, compilationUnit, problems);

        Set<String> childMethodNames = new HashSet<>();
        for (ASTMethod method : astObject.methods())
            declareMethod(method, objectScope, compilationUnit, problems, childMethodNames);

        for (ASTMethod method : astObject.methods()) {
            SemanticScope methodScope = new SemanticScope(objectScope);

            if (method.parameters() != null) {
                for (ASTParameter parameter : method.parameters()) {
                    resolveSymbolType(parameter.symbol(), parameter.line(), problems);
                    declareUnique(parameter.symbol(), methodScope, compilationUnit, problems, "parameter");
                }
            }

            for (ASTLocal local : method.locals()) {
                resolveSymbolType(local.symbol(), local.line(), problems);
                declareUnique(local.symbol(), methodScope, compilationUnit, problems, "local");
            }

            ensureImplicitReturn(method);
        }

        SemanticTypeChecker typeChecker = new SemanticTypeChecker(problems);
        typeChecker.check(astObject);

        return new SemanticAnalysisResult(new SemanticModel(astObject, objectScope), problems);
    }

    private void importParentSymbols(
            SemanticScope objectScope, CompilationUnit parentUnit, SemanticModel parentModel) {
        if (parentModel == null)
            return;

        ASTObject parentObject = parentModel.astObject();

        for (ASTField field : parentObject.fields())
            objectScope.declare(field.symbol(), parentUnit, true, null, null);

        for (ASTMethod method : parentObject.methods())
            objectScope.declare(method.symbol(), parentUnit, true, null, method);
    }

    private void declareField(
            ASTField field,
            SemanticScope objectScope,
            CompilationUnit compilationUnit,
            List<CompilationProblem> problems) {
        resolveSymbolType(field.symbol(), field.line(), problems);

        SemanticScope.SymbolBinding existing = objectScope.resolveLocally(field.symbol().name());
        if (existing != null && existing.method() != null) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Duplicate symbol '" + field.symbol().name() + "'; already declared as a method",
                            field.line()));
            return;
        }

        SemanticScope.SymbolBinding inheritedFrom = null;
        if (existing != null) {
            if (existing.inherited()) {
                // Field shadowing: keep the inherited binding for metadata but make the child field
                // the visible declaration.
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Warning: Field '" + field.symbol().name() + "' shadows inherited field",
                                field.line()));
                inheritedFrom = existing;
            } else if (existing.symbol() != field.symbol()) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Duplicate field '" + field.symbol().name() + "' in scope",
                                field.line()));
                return;
            }
        }

        objectScope.declare(field.symbol(), compilationUnit, false, inheritedFrom, null);
    }

    private void declareMethod(
            ASTMethod method,
            SemanticScope objectScope,
            CompilationUnit compilationUnit,
            List<CompilationProblem> problems,
            Set<String> childMethodNames) {
        resolveMethodSignatureTypes(method, problems);

        if (!childMethodNames.add(method.symbol().name())) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Duplicate method '" + method.symbol().name() + "' in child object",
                            method.line()));
        }

        SemanticScope.SymbolBinding existing = objectScope.resolveLocally(method.symbol().name());
        if (existing != null && existing.method() == null) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Duplicate symbol '" + method.symbol().name() + "'; already declared as a field",
                            method.line()));
            return;
        }

        SemanticScope.SymbolBinding inheritedFrom = null;
        if (existing != null && existing.inherited()) {
            inheritedFrom = existing;
            ASTMethod parentMethod = existing.method();

            if (!isOverrideCompatible(method, parentMethod)) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Override of method '" + method.symbol().name() + "' is incompatible with inherited declaration",
                                method.line()));
                return;
            }

            // Method override: attach the overridden declaration while keeping the child visible.
            method.setOverriddenMethod(parentMethod);
        } else if (existing != null && existing.symbol() != method.symbol()) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Duplicate method '" + method.symbol().name() + "' in child object",
                            method.line()));
            return;
        }

        SemanticScope.SymbolBinding binding =
                objectScope.declare(method.symbol(), compilationUnit, false, inheritedFrom, method);

        if (inheritedFrom != null && inheritedFrom.method() != null)
            binding.setOverriddenMethod(inheritedFrom.method());
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

    private void resolveMethodSignatureTypes(ASTMethod method, List<CompilationProblem> problems) {
        resolveSymbolType(method.symbol(), method.line(), problems);

        if (method.parameters() == null)
            return;

        for (ASTParameter parameter : method.parameters())
            resolveSymbolType(parameter.symbol(), parameter.line(), problems);
    }

    private void declareUnique(
            Symbol symbol,
            SemanticScope scope,
            CompilationUnit compilationUnit,
            List<CompilationProblem> problems,
            String kind) {
        SemanticScope.SymbolBinding existing = scope.resolveLocally(symbol.name());

        if (existing != null && existing.symbol() != symbol) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Duplicate " + kind + " '" + symbol.name() + "' in scope",
                            (Throwable) null));
            return;
        }

        scope.declare(symbol, compilationUnit);
    }

    private boolean isOverrideCompatible(ASTMethod child, ASTMethod parent) {
        if (parent == null)
            return true;

        boolean parametersMatch = matchingParameters(child.parameters(), parent.parameters());
        boolean returnCompatible = isTypeAssignable(parent.symbol().lpcType(), child.symbol().lpcType());
        return parametersMatch && returnCompatible;
    }

    private boolean matchingParameters(ASTParameters childParams, ASTParameters parentParams) {
        int childCount = (childParams != null) ? childParams.size() : 0;
        int parentCount = (parentParams != null) ? parentParams.size() : 0;

        if (childCount != parentCount)
            return false;

        for (int i = 0; i < childCount; i++) {
            LPCType childType = childParams.get(i).symbol().lpcType();
            LPCType parentType = parentParams.get(i).symbol().lpcType();

            if (!isTypeAssignable(parentType, childType) || !isTypeAssignable(childType, parentType))
                return false;
        }

        return true;
    }

    private boolean isTypeAssignable(LPCType expected, LPCType actual) {
        if (expected == null)
            return true;

        if (expected == LPCType.LPCMIXED)
            return true;

        if (actual == null)
            return false;

        if (actual == LPCType.LPCMIXED)
            return true;

        if ((expected == LPCType.LPCINT && actual == LPCType.LPCSTATUS)
                || (expected == LPCType.LPCSTATUS && actual == LPCType.LPCINT))
            return true;

        if (actual == LPCType.LPCNULL)
            return expected == LPCType.LPCOBJECT || expected == LPCType.LPCSTRING || expected == LPCType.LPCMIXED
                    || expected == LPCType.LPCARRAY || expected == LPCType.LPCMAPPING;

        return expected == actual;
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

}
