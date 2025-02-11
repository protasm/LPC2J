package io.github.protasm.lpc2j.parser.ast.visitor;

import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTMethods;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
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
	if (stmt.value() != null)
	    stmt.value().accept(this, lpcType);
    }
}
