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
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCall;
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
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class TypeInferenceVisitor implements ASTTypeVisitor {
    @Override
    public void visit(ASTArgument argument, LPCType lpcType) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void visit(ASTArguments arguments, LPCType lpcType) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void visit(ASTExprCall expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTExprFieldAccess expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTExprFieldStore expr, LPCType lpcType) {
	lpcType = expr.lpcType(); // pass down type of assignment target
	
	expr.field().accept(this, lpcType);
	expr.value().accept(this, lpcType);
    }

    @Override
    public void visit(ASTExprInvokeLocal expr, LPCType lpcType) {
	expr.setLPCType(lpcType);
    }

    @Override
    public void visit(ASTExprLiteralFalse expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTExprLiteralInteger expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTExprLiteralString expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTExprLiteralTrue expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTExprLocalAccess expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTExprLocalStore expr, LPCType lpcType) {
	lpcType = expr.lpcType(); // pass down type of assignment target
	
	expr.local().accept(this, lpcType);
	expr.value().accept(this, lpcType);
    }

    @Override
    public void visit(ASTExprNull expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTExprOpBinary expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTExprOpUnary expr, LPCType lpcType) {
    }

    @Override
    public void visit(ASTField field, LPCType lpcType) {
    }

    @Override
    public void visit(ASTFields fields, LPCType lpcType) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void visit(ASTLocal local, LPCType lpcType) {
    }

    @Override
    public void visit(ASTMethod method, LPCType lpcType) {
	lpcType = method.symbol().lpcType(); // pass down method return type
	
	method.body().accept(this, lpcType);
    }

    @Override
    public void visit(ASTMethods methods, LPCType lpcType) {
	for (ASTMethod method : methods)
	    method.accept(this, lpcType);
    }

    @Override
    public void visit(ASTObject object, LPCType lpcType) {
	object.methods().accept(this, lpcType);
    }

    @Override
    public void visit(ASTParameter parameter, LPCType lpcType) {
    }

    @Override
    public void visit(ASTParameters parameters, LPCType lpcType) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void visit(ASTStmtBlock stmt, LPCType lpcType) {
	for(ASTStatement statement : stmt)
	    statement.accept(this, lpcType);
    }

    @Override
    public void visit(ASTStmtExpression stmt, LPCType lpcType) {
	lpcType = null; // pass down null where expression value is ignored
	
	stmt.expression().accept(this, lpcType);
    }

    @Override
    public void visit(ASTStmtIfThenElse stmt, LPCType lpcType) {
	stmt.condition().accept(this, lpcType);
	stmt.thenBranch().accept(this, lpcType);
	
	if (stmt.elseBranch() != null)
	    stmt.elseBranch().accept(this, lpcType);
    }

    @Override
    public void visit(ASTStmtReturn stmt, LPCType lpcType) {
	if (stmt.value() != null)
	    stmt.value().accept(this, lpcType);
    }
}
