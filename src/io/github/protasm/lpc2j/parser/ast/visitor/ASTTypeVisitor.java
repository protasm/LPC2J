package io.github.protasm.lpc2j.parser.ast.visitor;

import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTFields;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTMethods;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.expr.*;
import io.github.protasm.lpc2j.parser.ast.stmt.*;
import io.github.protasm.lpc2j.parser.type.LPCType;

interface ASTTypeVisitor {
    void visit(ASTArgument argument, LPCType lpcType);
    void visit(ASTArguments arguments, LPCType lpcType);
    void visit(ASTExprCall expr, LPCType lpcType);
    void visit(ASTExprFieldAccess expr, LPCType lpcType);
    void visit(ASTExprFieldStore expr, LPCType lpcType);
    void visit(ASTExprInvokeLocal expr, LPCType lpcType);
    void visit(ASTExprLiteralFalse expr, LPCType lpcType);
    void visit(ASTExprLiteralInteger expr, LPCType lpcType);
    void visit(ASTExprLiteralString expr, LPCType lpcType);
    void visit(ASTExprLiteralTrue expr, LPCType lpcType);
    void visit(ASTExprLocalAccess expr, LPCType lpcType);
    void visit(ASTExprLocalStore expr, LPCType lpcType);
    void visit(ASTExprNull expr, LPCType lpcType);
    void visit(ASTExprOpBinary expr, LPCType lpcType);
    void visit(ASTExprOpUnary expr, LPCType lpcType);
    void visit(ASTField field, LPCType lpcType);
    void visit(ASTFields fields, LPCType lpcType);
    void visit(ASTLocal local, LPCType lpcType);
    void visit(ASTMethod method, LPCType lpcType);
    void visit(ASTMethods methods, LPCType lpcType);
    void visit(ASTObject object, LPCType lpcType);
    void visit(ASTParameter parameter, LPCType lpcType);
    void visit(ASTParameters parameters, LPCType lpcType);
    void visit(ASTStmtBlock stmt, LPCType lpcType);
    void visit(ASTStmtExpression stmt, LPCType lpcType);
    void visit(ASTStmtIfThenElse stmt, LPCType lpcType);
    void visit(ASTStmtReturn stmt, LPCType lpcType);
}
