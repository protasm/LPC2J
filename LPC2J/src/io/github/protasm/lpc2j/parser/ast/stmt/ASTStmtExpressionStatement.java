package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class ASTStmtExpressionStatement extends ASTStatement {
    private final ASTExpression expression;

    public ASTStmtExpressionStatement(int line, ASTExpression expression) {
	super(line);

	this.expression = expression;
    }

    public ASTExpression expression() {
	return expression;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	
	sb.append("\t\t\t\t");
	
	sb.append(String.format("%s(expr=%s)", className, expression));
	
	return sb.toString();
    }
}
