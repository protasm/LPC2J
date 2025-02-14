package io.github.protasm.lpc2j.parser.ast.expr;

import static io.github.protasm.lpc2j.parser.type.LPCType.LPCINT;
import static io.github.protasm.lpc2j.parser.type.LPCType.LPCSTATUS;
import static io.github.protasm.lpc2j.parser.type.LPCType.LPCSTRING;

import io.github.protasm.lpc2j.parser.ast.visitor.BytecodeVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprOpBinary extends ASTExpression {
    private final ASTExpression left;
    private final ASTExpression right;
    private final BinaryOpType operator;

    public ASTExprOpBinary(int line, ASTExpression left, ASTExpression right, BinaryOpType operator) {
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

    public BinaryOpType operator() {
	return operator;
    }

    @Override
    public LPCType lpcType() {
	switch (operator) {
	case BOP_ADD:
	    if (matchTypes(LPCINT, LPCINT))
		return LPCINT;
	    else if (matchTypes(LPCSTRING, LPCSTRING))
		return LPCSTRING;
	case BOP_SUB:
	case BOP_MULT:
	case BOP_DIV:
	    return LPCINT;
	case BOP_LT:
	case BOP_GT:
	    return LPCSTATUS; // Comparison expressions always evaluate to a boolean
	default:
	    throw new UnsupportedOperationException("Invalid operand types for operator " + operator);
	}
    }

    private boolean matchTypes(LPCType lType, LPCType rType) {
	return (left.lpcType() == lType) && (right.lpcType() == rType);
    }

    @Override
    public void accept(BytecodeVisitor visitor) {
	visitor.visit(this);
    }

    @Override
    public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
	visitor.visit(this, lpcType);
    }

    @Override
    public void accept(PrintVisitor visitor) {
	visitor.visit(this);
    }
}
