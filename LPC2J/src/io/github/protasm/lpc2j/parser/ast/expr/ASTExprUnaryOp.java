package io.github.protasm.lpc2j.parser.ast.expr;

import common.LTType;
import scanner.Token;

public class ASTExprUnaryOp extends ASTExpression {
    public enum Operator {
	NEGATE, NOT
    }

    private final ASTExpression operand;
    private final Operator operator;

    public ASTExprUnaryOp(Token startToken, ASTExpression operand, Operator operator) {
	super(startToken);

	this.operand = operand;
	this.operator = operator;
    }

    public ASTExpression operand() {
	return operand;
    }

    public Operator operator() {
	return operator;
    }

    @Override
    public LTType type() {
	switch (operator) {
	case NEGATE:
	    if (operand.type() == LTType.LT_INT)
		return LTType.LT_INT;
	    else
		throw new IllegalStateException("Unary '-' operator requires an integer operand.");
	case NOT:
	    if (operand.type() == LTType.LT_STATUS)
		return LTType.LT_STATUS;
	    else
		throw new IllegalStateException("Logical '!' operator requires a boolean operand.");
	default:
	    throw new UnsupportedOperationException("Unsupported operator: " + operator);
	}
    }

    @Override
    public String toString() {
	return String.format("ASTExprUnaryOp(operator=%s, operand=%s)", operator, operand);
    }
}
