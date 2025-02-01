package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalMethodInvoke;

public class ASTStmtInvocation extends ASTStatement {
    private final ASTExprLocalMethodInvoke invocation;

    public ASTStmtInvocation(int line, ASTExprLocalMethodInvoke invocation) {
	super(line);

	this.invocation = invocation;
    }

    public ASTExprLocalMethodInvoke methodInvocation() {
	return invocation;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s", className()));

	return sb.toString();
    }
}
