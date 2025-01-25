package io.github.protasm.lpc2j.parser.ast.stmt;

import ast.expr.ASTExpression;
import scanner.Token;

public class ASTStmtAssignment extends ASTStatement {
    private final String variable;
    private final ASTExpression value;

    public ASTStmtAssignment(Token startToken, String variable, ASTExpression value) {
	super(startToken);

	this.variable = variable;
	this.value = value;
    }

    public String variable() {
	return variable;
    }

    public ASTExpression value() {
	return value;
    }

    @Override
    public String toString() {
	return String.format("ASTStmtAssignment(variable=%s, value=%s)", variable, value);
    }
}
