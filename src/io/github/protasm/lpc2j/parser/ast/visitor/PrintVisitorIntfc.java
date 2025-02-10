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

interface PrintVisitorIntfc {
	void visit(ASTObject object);
	void visit(ASTField field);
	void visit(ASTFields fields);
	void visit(ASTMethod method);
	void visit(ASTMethods methods);
	void visit(ASTParameters parameters);
	void visit(ASTParameter parameter);
	void visit(ASTArguments arguments);
	void visit(ASTArgument argument);
	void visit(ASTLocal local);
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
