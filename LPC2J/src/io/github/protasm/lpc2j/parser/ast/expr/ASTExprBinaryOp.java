package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.LPCType;

import static io.github.protasm.lpc2j.LPCType.*;

public class ASTExprBinaryOp extends ASTExpression {
    public enum Operator {
	ADD, SUBTRACT, MULTIPLY, DIVIDE
    }

    private final ASTExpression left;
    private final ASTExpression right;
    private final Operator operator;

    public ASTExprBinaryOp(int line, ASTExpression left, ASTExpression right, Operator operator) {
	super(line);

	this.left = left;
	this.right = right;
	this.operator = operator;
    }

    public ASTExpression left() {
	return left;
    }

    public ASTExpression right() {
	return right;
    }

    public Operator operator() {
	return operator;
    }

    @Override
    public LPCType lpcType() {
	switch (operator) {
	case ADD:
	    if (matchTypes(LPCINT, LPCINT))
		return LPCINT;
	    else if (matchTypes(LPCSTRING, LPCSTRING))
		return LPCSTRING;
	default:
	    throw new UnsupportedOperationException("Invalid operand types for operator " + operator);
	}
    }

    private boolean matchTypes(LPCType lType, LPCType rType) {
	return left.lpcType() == lType && right.lpcType() == rType;
    }

    @Override
    public String toString() {
	return String.format("%s(operator=%s, left=%s, right=%s)", className, operator, left, right);
    }
}
