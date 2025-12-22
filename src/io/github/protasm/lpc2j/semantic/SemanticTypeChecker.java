package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.efun.EfunSignature;
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
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayLiteral;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralTrue;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprNull;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;
import java.util.List;
import java.util.Objects;

/**
 * Semantic type checker that validates expressions, arguments, and returns while refining symbol
 * types when they remain unspecified or {@code mixed}.
 */
public final class SemanticTypeChecker {
    private final List<CompilationProblem> problems;

    public SemanticTypeChecker(List<CompilationProblem> problems) {
        this.problems = Objects.requireNonNull(problems, "problems");
    }

    public void check(ASTObject object) {
        for (ASTMethod method : object.methods())
            checkMethod(method);
    }

    private void checkMethod(ASTMethod method) {
        if (method.body() == null)
            return;

        MethodContext context = new MethodContext(method);
        checkStatement(method.body(), context);
        context.finalizeReturn();
    }

    private void checkStatement(ASTStatement statement, MethodContext context) {
        if (statement == null)
            return;

        if (statement instanceof ASTStmtBlock block) {
            for (ASTStatement nested : block)
                checkStatement(nested, context);
            return;
        }

        if (statement instanceof ASTStmtExpression stmtExpression) {
            inferExpressionType(stmtExpression.expression(), context);
            return;
        }

        if (statement instanceof ASTStmtIfThenElse stmtIf) {
            // All expressions participate in truthiness; conditions are not restricted to booleans.
            inferExpressionType(stmtIf.condition(), context);
            checkStatement(stmtIf.thenBranch(), context);
            if (stmtIf.elseBranch() != null)
                checkStatement(stmtIf.elseBranch(), context);
            return;
        }

        if (statement instanceof ASTStmtReturn stmtReturn) {
            LPCType valueType = (stmtReturn.returnValue() != null)
                    ? inferExpressionType(stmtReturn.returnValue(), context)
                    : LPCType.LPCVOID;
            context.recordReturn(valueType, stmtReturn.isSynthetic(), stmtReturn.line());
            return;
        }

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Unsupported statement kind: " + statement.getClass().getSimpleName(),
                        statement.line()));
    }

    private LPCType inferExpressionType(ASTExpression expression, MethodContext context) {
        if (expression == null)
            return LPCType.LPCNULL;

        if (expression instanceof ASTExprLiteralInteger)
            return LPCType.LPCINT;
        if (expression instanceof ASTExprLiteralString)
            return LPCType.LPCSTRING;
        if (expression instanceof ASTExprLiteralTrue || expression instanceof ASTExprLiteralFalse)
            return LPCType.LPCSTATUS;
        if (expression instanceof ASTExprNull)
            return LPCType.LPCNULL;
        if (expression instanceof ASTExprArrayLiteral)
            return LPCType.LPCARRAY;

        if (expression instanceof ASTExprLocalAccess access)
            return valueType(access.local().symbol());

        if (expression instanceof ASTExprFieldAccess access)
            return valueType(access.field().symbol());

        if (expression instanceof ASTExprArrayAccess arrayAccess) {
            inferExpressionType(arrayAccess.target(), context);
            inferExpressionType(arrayAccess.index(), context);
            return LPCType.LPCMIXED;
        }

        if (expression instanceof ASTExprFieldStore store) {
            LPCType valueType = inferExpressionType(store.value(), context);
            LPCType fieldType = valueType(store.field().symbol());
            ensureAssignable(fieldType, valueType, store.line(), "Field assignment type mismatch");
            return fieldType(store.field().symbol(), valueType);
        }

        if (expression instanceof ASTExprLocalStore store) {
            LPCType valueType = inferExpressionType(store.value(), context);
            LPCType localType = valueType(store.local().symbol());
            ensureAssignable(localType, valueType, store.line(), "Local assignment type mismatch");
            return localType != null ? localType : valueType;
        }

        if (expression instanceof ASTExprArrayStore store) {
            inferExpressionType(store.target(), context);
            LPCType valueType = inferExpressionType(store.value(), context);
            ensureAssignable(LPCType.LPCARRAY, inferExpressionType(store.target(), context), store.line(), "Array assignment expects array target");
            return valueType;
        }

        if (expression instanceof ASTExprOpUnary unary)
            return inferUnaryType(unary, context);

        if (expression instanceof ASTExprOpBinary binary)
            return inferBinaryType(binary, context);

        if (expression instanceof ASTExprCallEfun callEfun)
            return inferEfunCall(callEfun, context);

        if (expression instanceof ASTExprCallMethod callMethod)
            return inferMethodCall(callMethod, context);

        if (expression instanceof ASTExprInvokeLocal invokeLocal) {
            inferArguments(invokeLocal.arguments(), null, context);
            invokeLocal.setLPCType(LPCType.LPCMIXED);
            return LPCType.LPCMIXED;
        }

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE,
                        "Unsupported expression kind: " + expression.getClass().getSimpleName(),
                        expression.line()));
        return LPCType.LPCMIXED;
    }

    private LPCType inferUnaryType(ASTExprOpUnary expr, MethodContext context) {
        // Logical negation is allowed on any type; rely on runtime truthiness.
        LPCType operandType = inferExpressionType(expr.right(), context);

        if (expr.operator() == UnaryOpType.UOP_NOT)
            return LPCType.LPCSTATUS;

        ensureAssignable(LPCType.LPCINT, operandType, expr.line(), "Unary operator expects numeric operand");
        return operandType != null ? operandType : LPCType.LPCINT;
    }

    private LPCType inferBinaryType(ASTExprOpBinary expr, MethodContext context) {
        LPCType leftType = inferExpressionType(expr.left(), context);
        LPCType rightType = inferExpressionType(expr.right(), context);
        BinaryOpType op = expr.operator();

        switch (op) {
        case BOP_ADD -> {
            if (leftType == LPCType.LPCARRAY || rightType == LPCType.LPCARRAY) {
                if (leftType != LPCType.LPCARRAY || rightType != LPCType.LPCARRAY) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "Array concatenation requires two arrays",
                                    expr.line()));
                }
                return LPCType.LPCARRAY;
            }
            if (leftType == LPCType.LPCSTRING || rightType == LPCType.LPCSTRING)
                return LPCType.LPCSTRING;

            ensureNumericOperands(leftType, rightType, expr.line(), "Addition expects numeric operands");
            return LPCType.LPCINT;
        }
        case BOP_SUB, BOP_MULT, BOP_DIV -> {
            ensureNumericOperands(leftType, rightType, expr.line(), op + " expects numeric operands");
            return LPCType.LPCINT;
        }
        case BOP_AND, BOP_OR -> {
            return LPCType.LPCSTATUS;
        }
        case BOP_GT, BOP_GE, BOP_LT, BOP_LE -> {
            ensureNumericOperands(leftType, rightType, expr.line(), "Comparison expects numeric operands");
            return LPCType.LPCSTATUS;
        }
        case BOP_EQ, BOP_NE -> {
            return LPCType.LPCSTATUS;
        }
        }

        return LPCType.LPCMIXED;
    }

    private LPCType inferEfunCall(ASTExprCallEfun expr, MethodContext context) {
        EfunSignature signature = expr.signature();

        if (signature == null) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Missing efun signature for call on line " + expr.line(),
                            expr.line()));
            inferArguments(expr.arguments(), null, context);
            return LPCType.LPCMIXED;
        }

        inferArguments(expr.arguments(), signature.parameterTypes(), context);
        return signature.returnType();
    }

    private LPCType inferMethodCall(ASTExprCallMethod expr, MethodContext context) {
        ASTParameters parameters = expr.method().parameters();
        inferArguments(expr.arguments(), parameters != null ? parameters.nodes().stream().map(ASTParameter::symbol).map(Symbol::lpcType).toList() : null, context);
        return valueType(expr.method().symbol());
    }

    private void inferArguments(ASTArguments arguments, List<LPCType> expectedTypes, MethodContext context) {
        if (arguments == null)
            return;

        for (int i = 0; i < arguments.size(); i++) {
            ASTArgument argument = arguments.get(i);
            LPCType actual = inferExpressionType(argument.expression(), context);
            LPCType expected = (expectedTypes != null && i < expectedTypes.size()) ? expectedTypes.get(i) : null;

            if (expected != null)
                ensureAssignable(expected, actual, argument.line(), "Argument " + (i + 1) + " type mismatch");
        }

        if (expectedTypes != null && arguments.size() != expectedTypes.size()) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.ANALYZE,
                            "Argument count mismatch: expected " + expectedTypes.size() + " but found " + arguments.size(),
                            arguments.line()));
        }
    }

    private void ensureNumericOperands(LPCType left, LPCType right, int line, String message) {
        ensureAssignable(LPCType.LPCINT, left, line, message);
        ensureAssignable(LPCType.LPCINT, right, line, message);
    }

    private void ensureAssignable(LPCType expected, LPCType actual, int line, String message) {
        if (expected == null || isTypeAssignable(expected, actual))
            return;

        problems.add(
                new CompilationProblem(
                        CompilationStage.ANALYZE,
                        message + " (expected " + expected + " but found " + actual + ")",
                        line));
    }

    private boolean isTypeAssignable(LPCType expected, LPCType actual) {
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
                    || expected == LPCType.LPCARRAY;

        return expected == actual;
    }

    private LPCType valueType(Symbol symbol) {
        return (symbol != null) ? symbol.lpcType() : LPCType.LPCMIXED;
    }

    private LPCType fieldType(Symbol symbol, LPCType candidate) {
        if (symbol == null)
            return candidate != null ? candidate : LPCType.LPCMIXED;

        refineSymbol(symbol, candidate);
        return symbol.lpcType();
    }

    private void refineSymbol(Symbol symbol, LPCType candidate) {
        if (symbol == null || candidate == null)
            return;

        LPCType declared = symbol.declaredType();
        LPCType existing = symbol.lpcType();

        if (declared != null && declared != LPCType.LPCMIXED && declared != LPCType.LPCNULL)
            return;

        if (existing == null || existing == LPCType.LPCMIXED)
            symbol.setLpcType(candidate);
    }

    private final class MethodContext {
        private final ASTMethod method;
        private LPCType inferredReturn;

        private MethodContext(ASTMethod method) {
            this.method = method;
        }

        void recordReturn(LPCType valueType, boolean synthetic, int line) {
            LPCType declared = method.symbol().lpcType();

            if (valueType == LPCType.LPCVOID) {
                if (declared != null && declared != LPCType.LPCVOID && declared != LPCType.LPCMIXED) {
                    problems.add(
                            new CompilationProblem(
                                    CompilationStage.ANALYZE,
                                    "Non-void methods must return a value of type " + declared + ".",
                                    line));
                }
                return;
            }

            if (synthetic && (declared == null || declared == LPCType.LPCMIXED))
                return;

            if (declared != null && declared != LPCType.LPCMIXED && declared != LPCType.LPCNULL) {
                ensureAssignable(declared, valueType, line, "Return type mismatch");
                return;
            }

            inferredReturn = mergeReturn(inferredReturn, valueType);
        }

        void finalizeReturn() {
            LPCType declared = method.symbol().lpcType();
            if ((declared == null || declared == LPCType.LPCMIXED) && inferredReturn != null)
                method.symbol().setLpcType(inferredReturn);
        }

        private LPCType mergeReturn(LPCType existing, LPCType candidate) {
            if (existing == null)
                return candidate;
            if (candidate == null)
                return existing;
            if (existing == candidate)
                return existing;
            return LPCType.LPCMIXED;
        }
    }
}
