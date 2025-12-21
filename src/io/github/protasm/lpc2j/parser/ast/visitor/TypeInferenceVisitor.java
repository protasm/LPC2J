package io.github.protasm.lpc2j.parser.ast.visitor;

import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTFields;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTMethods;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
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
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;

/** Propagates contextual type hints through the AST to enrich symbol metadata. */
public final class TypeInferenceVisitor implements ASTVisitor {
    private Symbol currentMethodSymbol;
    private LPCType expectedType;

    public void visitWithExpectedType(ASTObject astObject, LPCType lpcType) {
        withExpectation(lpcType, () -> astObject.accept(this));
    }

    @Override
    public void visitExprFieldStore(ASTExprFieldStore expr) {
        LPCType assignmentType = expr.lpcType();
        withExpectation(assignmentType, () -> expr.field().accept(this));
        withExpectation(assignmentType, () -> expr.value().accept(this));
        updateSymbolType(expr.field().symbol(), expr.value().lpcType());
    }

    @Override
    public void visitExprInvokeLocal(ASTExprInvokeLocal expr) {
        expr.setLPCType(expectedType);
    }

    @Override
    public void visitExprLocalStore(ASTExprLocalStore expr) {
        LPCType assignmentType = expr.lpcType();
        withExpectation(assignmentType, () -> expr.local().accept(this));
        withExpectation(assignmentType, () -> expr.value().accept(this));
        updateSymbolType(expr.local().symbol(), expr.value().lpcType());
    }

    @Override
    public void visitMethod(ASTMethod method) {
        Symbol prevMethodSymbol = currentMethodSymbol;
        currentMethodSymbol = method.symbol();
        withExpectation(currentMethodSymbol.lpcType(), () -> method.body().accept(this));
        currentMethodSymbol = prevMethodSymbol;
    }

    @Override
    public void visitMethods(ASTMethods methods) {
        for (ASTMethod method : methods)
            method.accept(this);
    }

    @Override
    public void visitObject(ASTObject object) {
        object.methods().accept(this);
    }

    @Override
    public void visitStmtBlock(ASTStmtBlock stmt) {
        for (ASTStatement statement : stmt)
            statement.accept(this);
    }

    @Override
    public void visitStmtExpression(ASTStmtExpression stmt) {
        withExpectation(null, () -> stmt.expression().accept(this));
    }

    @Override
    public void visitStmtIfThenElse(ASTStmtIfThenElse stmt) {
        withExpectation(LPCType.LPCSTATUS, () -> stmt.condition().accept(this));
        stmt.thenBranch().accept(this);
        if (stmt.elseBranch() != null)
            stmt.elseBranch().accept(this);
    }

    @Override
    public void visitStmtReturn(ASTStmtReturn stmt) {
        if (stmt.returnValue() != null) {
            withExpectation(expectedType, () -> stmt.returnValue().accept(this));
            updateMethodType(stmt.returnValue().lpcType());
        }
    }

    @Override
    public void visitArguments(ASTArguments astArguments) {
        for (ASTArgument arg : astArguments)
            arg.accept(this);
    }

    @Override
    public void visitExprFieldAccess(ASTExprFieldAccess astExprFieldAccess) {
        updateSymbolType(astExprFieldAccess.field().symbol(), expectedType);
    }

    @Override
    public void visitExprCallMethod(ASTExprCallMethod astExprCall) {
        astExprCall.arguments().accept(this);
    }

    @Override
    public void visitExprCallEfun(ASTExprCallEfun astExprCallEfun) {
        astExprCallEfun.arguments().accept(this);
    }

    @Override
    public void visitExprLiteralFalse(ASTExprLiteralFalse expr) {
    }

    @Override
    public void visitExprLiteralInteger(ASTExprLiteralInteger expr) {
    }

    @Override
    public void visitExprLiteralString(ASTExprLiteralString expr) {
    }

    @Override
    public void visitExprLiteralTrue(ASTExprLiteralTrue expr) {
    }

    @Override
    public void visitExprLocalAccess(ASTExprLocalAccess astExprLocalAccess) {
        updateSymbolType(astExprLocalAccess.local().symbol(), expectedType);
    }

    @Override
    public void visitExprNull(ASTExprNull expr) {
    }

    @Override
    public void visitExprOpBinary(ASTExprOpBinary expr) {
        LPCType operandExpectation = expectedBinaryOperandType(expr, expectedType);
        withExpectation(operandExpectation, () -> expr.left().accept(this));
        withExpectation(operandExpectation, () -> expr.right().accept(this));
    }

    @Override
    public void visitExprOpUnary(ASTExprOpUnary expr) {
        LPCType operandExpectation =
                expr.operator() == UnaryOpType.UOP_NOT ? LPCType.LPCSTATUS : LPCType.LPCINT;
        withExpectation(operandExpectation, () -> expr.right().accept(this));
    }

    @Override
    public void visitArgument(ASTArgument astArgument) {
        astArgument.expression().accept(this);
    }

    @Override
    public void visitField(ASTField astField) {
        updateSymbolType(astField.symbol(), expectedType);
    }

    @Override
    public void visitFields(ASTFields astFields) {}

    @Override
    public void visitParameter(ASTParameter astParameter) {
        updateSymbolType(astParameter.symbol(), expectedType);
    }

    @Override
    public void visitParameters(ASTParameters astParameters) {
        for (ASTParameter param : astParameters)
            withExpectation(param.symbol().lpcType(), () -> param.accept(this));
    }

    private LPCType expectedBinaryOperandType(ASTExprOpBinary expr, LPCType context) {
        return switch (expr.operator()) {
        case BOP_ADD -> (expr.left().lpcType() == LPCType.LPCSTRING || expr.right().lpcType() == LPCType.LPCSTRING
                || context == LPCType.LPCSTRING) ? LPCType.LPCSTRING : LPCType.LPCINT;
        case BOP_SUB, BOP_MULT, BOP_DIV, BOP_GT, BOP_GE, BOP_LT, BOP_LE, BOP_EQ, BOP_NE, BOP_AND, BOP_OR -> LPCType.LPCINT;
        default -> context;
        };
    }

    private void updateSymbolType(Symbol symbol, LPCType candidate) {
        if (symbol == null || candidate == null)
            return;

        if (symbol.declaredTypeName() == null && symbol.declaredType() == LPCType.LPCMIXED)
            return;

        LPCType existing = symbol.lpcType();

        if (candidate == LPCType.LPCSTATUS
                && existing != null
                && existing != LPCType.LPCMIXED
                && existing != LPCType.LPCNULL
                && existing != LPCType.LPCSTATUS)
            return;

        symbol.setLpcType(candidate);
    }

    private void updateMethodType(LPCType candidate) {
        if (currentMethodSymbol == null || candidate == null)
            return;

        LPCType declared = currentMethodSymbol.declaredType();

        if (declared != null && declared != LPCType.LPCMIXED && declared != LPCType.LPCNULL)
            return;

        updateSymbolType(currentMethodSymbol, candidate);
    }

    private void withExpectation(LPCType lpcType, Runnable runnable) {
        LPCType prev = expectedType;
        expectedType = lpcType;
        runnable.run();
        expectedType = prev;
    }
}
