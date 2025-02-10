package io.github.protasm.lpc2j.parser.ast.visitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.expr.*;
import io.github.protasm.lpc2j.parser.ast.stmt.*;

interface TypeInferenceVisitorIntfc {
	void visit(ASTObject object, LPCType lpcType);

	void visit(ASTField field, LPCType lpcType);

	void visit(ASTMethod method, LPCType lpcType);

	void visit(ASTParameter parameter, LPCType lpcType);

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

	void visit(ASTStmtBlock stmt, LPCType lpcType);

	void visit(ASTStmtExpression stmt, LPCType lpcType);

	void visit(ASTStmtIfThenElse stmt, LPCType lpcType);

	void visit(ASTStmtReturn stmt, LPCType lpcType);
}
