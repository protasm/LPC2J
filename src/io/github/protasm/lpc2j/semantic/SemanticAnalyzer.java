package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationStage;
import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.ast.visitor.ASTVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        if (astObject == null)
            throw new IllegalArgumentException("ASTObject cannot be null.");

        List<CompilationProblem> problems = new ArrayList<>();
        SemanticScope objectScope = new SemanticScope();

        for (ASTField field : astObject.fields()) {
            resolveSymbolType(field.symbol(), field.line(), problems);
            objectScope.declare(field.symbol());
        }

        for (ASTMethod method : astObject.methods()) {
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
        new TypeInferenceVisitor().visitWithExpectedType(astObject, LPCType.LPCNULL);
        new EfunValidationVisitor(problems).visitObject(astObject);

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
                                null));
            }

            symbol.resolveDeclaredType(LPCType.LPCMIXED);
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

    private void validateReturns(ASTStatement statement, LPCType expected, List<CompilationProblem> problems) {
        if (statement instanceof ASTStmtReturn stmtReturn) {
            checkReturnCompatibility(stmtReturn, expected, problems);
        } else if (statement instanceof ASTStmtIfThenElse stmtIf) {
            validateReturns(stmtIf.thenBranch(), expected, problems);

            if (stmtIf.elseBranch() != null)
                validateReturns(stmtIf.elseBranch(), expected, problems);
        } else if (statement instanceof ASTStmtExpression) {
            // Expressions are validated by the type inference visitor.
        } else if (statement instanceof ASTStmtBlock block) {
            for (ASTStatement nested : block) {
                validateReturns(nested, expected, problems);
            }
        }
    }

    private void ensureImplicitReturn(ASTMethod method) {
        if (method.body() == null)
            return;

        List<ASTStatement> statements = method.body().statements();

        if (statements.isEmpty() || !(statements.get(statements.size() - 1) instanceof ASTStmtReturn))
            statements.add(new ASTStmtReturn(method.body().line(), defaultReturnValue(method)));
    }

    private ASTExpression defaultReturnValue(ASTMethod method) {
        if (method.symbol().lpcType() == LPCType.LPCVOID)
            return null;

        return new ASTExprLiteralInteger(
                method.body().line(), new Token<>(TokenType.T_INT_LITERAL, "0", 0, null));
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
        return isTypeAssignable(expected, actual);
    }

    private boolean isTypeAssignable(LPCType expected, LPCType actual) {
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

    private final class EfunValidationVisitor implements ASTVisitor {
        private final List<CompilationProblem> problems;

        private EfunValidationVisitor(List<CompilationProblem> problems) {
            this.problems = problems;
        }

        @Override
        public void visitObject(ASTObject object) {
            object.methods().accept(this);
        }

        @Override
        public void visitMethods(io.github.protasm.lpc2j.parser.ast.ASTMethods methods) {
            for (ASTMethod method : methods)
                method.accept(this);
        }

        @Override
        public void visitMethod(ASTMethod method) {
            if (method.body() != null)
                method.body().accept(this);
        }

        @Override
        public void visitStmtBlock(ASTStmtBlock stmt) {
            for (ASTStatement nested : stmt)
                nested.accept(this);
        }

        @Override
        public void visitStmtExpression(ASTStmtExpression stmt) {
            if (stmt.expression() != null)
                stmt.expression().accept(this);
        }

        @Override
        public void visitStmtIfThenElse(ASTStmtIfThenElse stmt) {
            if (stmt.condition() != null)
                stmt.condition().accept(this);
            if (stmt.thenBranch() != null)
                stmt.thenBranch().accept(this);
            if (stmt.elseBranch() != null)
                stmt.elseBranch().accept(this);
        }

        @Override
        public void visitStmtReturn(ASTStmtReturn stmt) {
            if (stmt.returnValue() != null)
                stmt.returnValue().accept(this);
        }

        @Override
        public void visitExprCallEfun(ASTExprCallEfun expr) {
            if (expr.signature() == null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Missing efun signature for call on line " + expr.line(),
                                null));
                return;
            }

            validateEfunCall(expr);
            expr.arguments().accept(this);
        }

        @Override
        public void visitExprCallMethod(ASTExprCallMethod expr) {
            expr.arguments().accept(this);
        }

        @Override
        public void visitExprFieldStore(ASTExprFieldStore expr) {
            if (expr.value() != null)
                expr.value().accept(this);
        }

        @Override
        public void visitExprInvokeLocal(ASTExprInvokeLocal expr) {
            if (expr.arguments() != null)
                expr.arguments().accept(this);
        }

        @Override
        public void visitExprLocalStore(ASTExprLocalStore expr) {
            if (expr.value() != null)
                expr.value().accept(this);
        }

        @Override
        public void visitExprOpBinary(ASTExprOpBinary expr) {
            if (expr.left() != null)
                expr.left().accept(this);
            if (expr.right() != null)
                expr.right().accept(this);
        }

        @Override
        public void visitExprOpUnary(ASTExprOpUnary expr) {
            if (expr.right() != null)
                expr.right().accept(this);
        }

        @Override
        public void visitArguments(ASTArguments arguments) {
            for (ASTArgument argument : arguments)
                argument.accept(this);
        }

        @Override
        public void visitArgument(ASTArgument argument) {
            if (argument.expression() != null)
                argument.expression().accept(this);
        }

        private void validateEfunCall(ASTExprCallEfun expr) {
            var signature = expr.signature();
            int arity = signature.arity();

            if (expr.arguments().size() != arity) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.ANALYZE,
                                "Efun '" + signature.name() + "' expects " + arity + " argument(s).",
                                null));
                return;
            }

            for (int i = 0; i < arity; i++) {
                LPCType expected = signature.parameterTypes().get(i);
                LPCType actual = expr.arguments().get(i).expression().lpcType();

                if (expected != null && !isTypeAssignable(expected, actual)) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "Argument " + (i + 1) + " of efun '" + signature.name() + "' expects "
                                            + expected + " but found " + actual + ".",
                                    null));
                }
            }
        }
    }
}
