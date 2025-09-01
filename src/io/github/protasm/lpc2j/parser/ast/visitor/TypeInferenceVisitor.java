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
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class TypeInferenceVisitor {
	public void visit(ASTExprFieldStore expr, LPCType lpcType) {
		lpcType = expr.lpcType(); // pass down type of assignment target

		expr.field().accept(this, lpcType);
		expr.value().accept(this, lpcType);
	}

	public void visit(ASTExprInvokeLocal expr, LPCType lpcType) {
		expr.setLPCType(lpcType);
	}

	public void visit(ASTExprLocalStore expr, LPCType lpcType) {
		lpcType = expr.lpcType(); // pass down type of assignment target

		expr.local().accept(this, lpcType);
		expr.value().accept(this, lpcType);
	}

	public void visit(ASTMethod method, LPCType lpcType) {
		lpcType = method.symbol().lpcType(); // pass down method return type

		method.body().accept(this, lpcType);
	}

	public void visit(ASTMethods methods, LPCType lpcType) {
		for (ASTMethod method : methods)
			method.accept(this, lpcType);
	}

	public void visit(ASTObject object, LPCType lpcType) {
		object.methods().accept(this, lpcType);
	}

	public void visit(ASTStmtBlock stmt, LPCType lpcType) {
		for (ASTStatement statement : stmt)
			statement.accept(this, lpcType);
	}

	public void visit(ASTStmtExpression stmt, LPCType lpcType) {
		lpcType = null; // pass down null where expression value is ignored

		stmt.expression().accept(this, lpcType);
	}

	public void visit(ASTStmtIfThenElse stmt, LPCType lpcType) {
		stmt.condition().accept(this, lpcType);
		stmt.thenBranch().accept(this, lpcType);

		if (stmt.elseBranch() != null)
			stmt.elseBranch().accept(this, lpcType);
	}

	public void visit(ASTStmtReturn stmt, LPCType lpcType) {
		if (stmt.returnValue() != null)
			stmt.returnValue().accept(this, lpcType);
	}

	public void visit(ASTArguments astArguments, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprFieldAccess astExprFieldAccess, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprCallMethod astExprCall, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprCallEfun astExprCallEfun, LPCType lpcTyp) {

	}

	public void visit(ASTExprLiteralFalse astExprLiteralFalse, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprLiteralInteger astExprLiteralInteger, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprLiteralString astExprLiteralString, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprLiteralTrue astExprLiteralTrue, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprLocalAccess astExprLocalAccess, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprNull astExprNull, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprOpBinary astExprOpBinary, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTExprOpUnary astExprOpUnary, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTArgument astArgument, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTField astField, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTFields astFields, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTParameter astParameter, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	public void visit(ASTParameters astParameters, LPCType lpcType) {
		// TODO Auto-generated method stub

	}
}
