package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.LPCType;

public class ASTExprUnaryOp extends ASTExpression {
    public enum Operator {
	NEGATE, NOT
    }

    private final ASTExpression operand;
    private final Operator operator;

    public ASTExprUnaryOp(int line, ASTExpression operand, Operator operator) {
	super(line);

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
    public LPCType lpcType() {
	switch (operator) {
	case NEGATE:
	    if (operand.lpcType() == LPCType.LPCINT)
		return LPCType.LPCINT;
	    else
		throw new IllegalStateException("Unary '-' operator requires an integer operand.");
	case NOT:
	    if (operand.lpcType() == LPCType.LPCSTATUS)
		return LPCType.LPCSTATUS;
	    else
		throw new IllegalStateException("Logical '!' operator requires a boolean operand.");
	default:
	    throw new UnsupportedOperationException("Unsupported operator: " + operator);
	}
    }

    @Override
    public String toString() {
	return String.format("%s(operator=%s, operand=%s)", className, operator, operand);
    }
}
