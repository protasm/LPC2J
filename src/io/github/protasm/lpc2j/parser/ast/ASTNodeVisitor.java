package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.ast.expr.*;
import io.github.protasm.lpc2j.parser.ast.stmt.*;

interface ASTNodeVisitor {
    void visit(ASTObject object);
    void visit(ASTField field);
    void visit(ASTMethod method);
    void visit(ASTParameter parameter);
    void visit(ASTExprCall expr);
    void visit(ASTExprFieldAccess expr);
    void visit(ASTExprFieldStore expr);
    void visit(ASTExprInvokeLocal expr);
    void visit(ASTExprLiteralFalse expr);
    void visit(ASTExprLiteralInteger expr);
    void visit(ASTExprLiteralString expr);
    void visit(ASTExprLiteralTrue expr);
    void visit(ASTExprLocalAccess expr);
    void visit(ASTExprLocalStore expr);
    void visit(ASTExprNull expr);
    void visit(ASTExprOpBinary expr);
    void visit(ASTExprOpUnary expr);
    void visit(ASTStmtBlock stmt);
    void visit(ASTStmtExpression stmt);
    void visit(ASTStmtIfThenElse stmt);
    void visit(ASTStmtReturn stmt);
}
