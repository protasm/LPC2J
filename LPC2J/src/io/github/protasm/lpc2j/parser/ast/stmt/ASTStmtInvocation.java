package io.github.protasm.lpc2j.parser.ast.stmt;

import ast.expr.ASTExprInvocation;
import scanner.Token;

public class ASTStmtInvocation extends ASTStatement {
    private final ASTExprInvocation invocation;

    public ASTStmtInvocation(Token startToken, ASTExprInvocation invocation) {
	super(startToken);

	this.invocation = invocation;
    }

    public ASTExprInvocation methodInvocation() {
	return invocation;
    }

    @Override
    public String toString() {
	return String.format("ASTStmtInvocation(invocation=%s)", invocation);
    }
}
