package io.github.protasm.lpc2j.parser.ast.stmt;

import ast.expr.ASTExpression;
import scanner.Token;

public class ASTStmtExpressionStatement extends ASTStatement {
    private final ASTExpression expression;

    public ASTStmtExpressionStatement(Token startToken, ASTExpression expression) {
	super(startToken);

	this.expression = expression;
    }

    public ASTExpression expression() {
	return expression;
    }

    // String representation for debugging
    @Override
    public String toString() {
	return String.format("ASTStmtExpressionStatement(expression=%s)", expression);
    }
}
