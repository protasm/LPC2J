package io.github.protasm.lpc2j.parser.ast.expr;

import common.LTType;
import scanner.Token;

public class ASTExprBinaryOp extends ASTExpression {
    public enum Operator {
	ADD, SUBTRACT, MULTIPLY, DIVIDE
    }

    private final ASTExpression left;
    private final ASTExpression right;
    private final Operator operator;

    public ASTExprBinaryOp(Token startToken, ASTExpression left, ASTExpression right, Operator operator) {
	super(startToken);

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
    public LTType type() {
	switch (operator) {
	case ADD:
	    if (left.type() == LTType.LT_INT && left.type() == LTType.LT_INT) {
		return LTType.LT_INT;
	    } else if (left.type() == LTType.LT_STRING && left.type() == LTType.LT_STRING) {
		return LTType.LT_STRING;
	    }
	default:
	    throw new UnsupportedOperationException("Invalid operand types for operator " + operator);
	}
    }

    @Override
    public String toString() {
	return String.format("ASTExprBinaryOp(operator=%s, left=%s, right=%s)", operator, left, right);
    }
}
