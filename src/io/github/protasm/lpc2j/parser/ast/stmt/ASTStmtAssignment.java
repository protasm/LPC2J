package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class ASTStmtAssignment extends ASTStatement {
    private final String variable;
    private final ASTExpression value;

    public ASTStmtAssignment(int line, String variable, ASTExpression value) {
	super(line);

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
	return String.format("%s(variable=%s, value=%s)", className, variable, value);
    }
}
