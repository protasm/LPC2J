package io.github.protasm.lpc2j.parser.ast.visitor;

import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
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
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class TypeInferenceVisitor implements TypeInferenceVisitorIntfc {
	@Override
	public void visit(ASTObject object, LPCType lpcType) {
	}

	@Override
	public void visit(ASTField field, LPCType lpcType) {
	}

	@Override
	public void visit(ASTMethod method, LPCType lpcType) {
	}

	@Override
	public void visit(ASTParameter parameter, LPCType lpcType) {
	}

	@Override
	public void visit(ASTExprCall expr, LPCType lpcType) {
	}

	@Override
	public void visit(ASTExprFieldAccess expr, LPCType lpcType) {
	}

	@Override
	public void visit(ASTExprFieldStore expr, LPCType lpcType) {
	}

	@Override
	public void visit(ASTExprInvokeLocal expr, LPCType lpcType) {
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
	public void visit(ASTStmtBlock stmt, LPCType lpcType) {
	}

	@Override
	public void visit(ASTStmtExpression stmt, LPCType lpcType) {
	}

	@Override
	public void visit(ASTStmtIfThenElse stmt, LPCType lpcType) {
	}

	@Override
	public void visit(ASTStmtReturn stmt, LPCType lpcType) {
	}

	@Override
	public void visit(ASTLocal local, LPCType lpcType) {
	}
}
