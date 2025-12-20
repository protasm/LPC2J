package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationStage;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Performs semantic analysis on a parsed AST and produces a typed model. */
public final class SemanticAnalyzer {
    private final TypeResolver typeResolver;

    public SemanticAnalyzer() {
        this(new TypeResolver());
    }

    public SemanticAnalyzer(TypeResolver typeResolver) {
        this.typeResolver = Objects.requireNonNull(typeResolver, "typeResolver");
    }

    public SemanticAnalysisResult analyze(ASTObject astObject) {
        if (astObject == null)
            throw new IllegalArgumentException("ASTObject cannot be null.");

        List<CompilationProblem> problems = new ArrayList<>();
        SemanticScope objectScope = new SemanticScope();

        for (ASTField field : astObject.fields().values()) {
            resolveSymbolType(field.symbol(), field.line(), problems);
            objectScope.declare(field.symbol());
        }

        for (ASTMethod method : astObject.methods().values()) {
            resolveSymbolType(method.symbol(), method.line(), problems);
            objectScope.declare(method.symbol());

            SemanticScope methodScope = new SemanticScope(objectScope);

            if (method.parameters() != null) {
                for (ASTParameter parameter : method.parameters()) {
                    resolveSymbolType(parameter.symbol(), parameter.line(), problems);
                    declareUnique(parameter.symbol(), methodScope, problems, "parameter");
                }
            }

            for (ASTLocal local : method.locals()) {
                resolveSymbolType(local.symbol(), local.line(), problems);
                declareUnique(local.symbol(), methodScope, problems, "local");
            }

            ensureImplicitReturn(method);

            if ((method.body() != null) && (method.symbol().lpcType() != null))
                validateReturns(method.body(), method.symbol().lpcType(), problems);
        }

        // Run the legacy type inference pass after declared types have been resolved so that it
        // can propagate contextual information without depending on scanner classification.
        astObject.accept(new TypeInferenceVisitor(), LPCType.LPCNULL);

        return new SemanticAnalysisResult(new SemanticModel(astObject, objectScope), problems);
    }

    private void resolveSymbolType(Symbol symbol, int line, List<CompilationProblem> problems) {
        if (symbol == null)
            return;

        if (symbol.lpcType() != null)
            return;

        LPCType resolved = typeResolver.resolve(symbol.declaredTypeName());

        if (resolved == null) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Unknown type '" + symbol.declaredTypeName() + "' for symbol '" + symbol.name() + "'",
                            null));
            return;
        }

        symbol.resolveDeclaredType(resolved);
    }

    private void declareUnique(
            Symbol symbol, SemanticScope scope, List<CompilationProblem> problems, String kind) {
        Symbol existing = scope.resolve(symbol.name());

        if (existing != null && existing != symbol) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Duplicate " + kind + " '" + symbol.name() + "' in scope",
                            null));
            return;
        }

        scope.declare(symbol);
    }

    private void validateReturns(ASTStmtBlock block, LPCType expected, List<CompilationProblem> problems) {
        for (ASTStatement statement : block) {
            if (statement instanceof ASTStmtReturn stmtReturn) {
                checkReturnCompatibility(stmtReturn, expected, problems);
            } else if (statement instanceof ASTStmtIfThenElse stmtIf) {
                validateReturns(stmtIf.thenBranch(), expected, problems);

                if (stmtIf.elseBranch() != null)
                    validateReturns(stmtIf.elseBranch(), expected, problems);
            } else if (statement instanceof ASTStmtExpression) {
                // Expressions are validated by the type inference visitor.
            } else if (statement instanceof ASTStmtBlock nested) {
                validateReturns(nested, expected, problems);
            }
        }
    }

    private void ensureImplicitReturn(ASTMethod method) {
        if (method.body() == null || method.symbol().lpcType() != LPCType.LPCVOID)
            return;

        List<ASTStatement> statements = method.body().statements();

        if (statements.isEmpty() || !(statements.get(statements.size() - 1) instanceof ASTStmtReturn))
            statements.add(new ASTStmtReturn(method.body().line(), null));
    }

    private void checkReturnCompatibility(
            ASTStmtReturn stmtReturn, LPCType expected, List<CompilationProblem> problems) {
        ASTExpression returnValue = stmtReturn.returnValue();

        if (returnValue == null) {
            if (expected != null && expected != LPCType.LPCVOID) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Non-void methods must return a value of type " + expected + ".",
                                null));
            }
            return;
        }

        if (expected == null || expected == LPCType.LPCMIXED)
            return;

        if (isReturnTypeCompatible(expected, returnValue.lpcType()))
            return;

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Return type mismatch: expected " + expected + " but found " + returnValue.lpcType() + ".",
                        null));
    }

    private boolean isReturnTypeCompatible(LPCType expected, LPCType actual) {
        if (expected == LPCType.LPCMIXED)
            return true;

        if (actual == null)
            return false;

        if ((expected == LPCType.LPCINT && actual == LPCType.LPCSTATUS)
                || (expected == LPCType.LPCSTATUS && actual == LPCType.LPCINT))
            return true;

        if (actual == LPCType.LPCNULL)
            return expected == LPCType.LPCOBJECT || expected == LPCType.LPCSTRING || expected == LPCType.LPCMIXED;

        return expected == actual;
    }
}
