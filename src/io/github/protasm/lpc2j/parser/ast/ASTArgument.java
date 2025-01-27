package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class ASTArgument extends ASTNode {
    private final ASTExpression expression;

    public ASTArgument(int line, ASTExpression expression) {
	super(line);

	this.expression = expression;
    }

    public ASTExpression expression() {
	return expression;
    }

    @Override
    public String dump(int level) {
	StringBuilder sb = new StringBuilder();

	sb.append(" ".repeat(level * 2));

	sb.append(String.format("%s(expression=%s)\n", className(), expression.dump(0)));

	return sb.toString();
    }
}
