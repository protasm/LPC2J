package io.github.protasm.lpc2j.parser.ast;

import ast.expr.ASTExpression;
import scanner.Token;

public class ASTArgument extends ASTNode {
    private final ASTExpression expression;

    public ASTArgument(Token startToken, ASTExpression expression) {
	super(startToken);

	this.expression = expression;
    }

    public ASTExpression expression() {
	return expression;
    }

    @Override
    public String toString() {
	return String.format("ASTArgument(expression=%s)", expression);
    }
}
