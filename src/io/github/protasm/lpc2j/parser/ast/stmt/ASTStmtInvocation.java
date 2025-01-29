package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvocation;

public class ASTStmtInvocation extends ASTStatement {
	private final ASTExprInvocation invocation;

	public ASTStmtInvocation(int line, ASTExprInvocation invocation) {
		super(line);

		this.invocation = invocation;
	}

	public ASTExprInvocation methodInvocation() {
		return invocation;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s(invocation=%s)", className(), invocation));

		return sb.toString();
	}
}
