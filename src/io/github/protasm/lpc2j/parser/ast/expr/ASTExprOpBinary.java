package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.BinaryOpType;
import io.github.protasm.lpc2j.LPCType;

import static org.objectweb.asm.Opcodes.*;
import static io.github.protasm.lpc2j.LPCType.*;

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

	default:
	    throw new UnsupportedOperationException("Invalid operand types for operator " + operator);
	}
    }

    private boolean matchTypes(LPCType lType, LPCType rType) {
	return left.lpcType() == lType && right.lpcType() == rType;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	left.toBytecode(mv);
	right.toBytecode(mv);

	switch (operator) {
	case BOP_ADD:
	    mv.visitInsn(IADD);
	    break;
	case BOP_SUB:
	    mv.visitInsn(ISUB);
	    break;
	case BOP_MULT:
	    mv.visitInsn(IMUL);
	    break;
	case BOP_DIV:
	    mv.visitInsn(IDIV);
	    break;
	default:
	    throw new UnsupportedOperationException("Unsupported operator: " + operator);
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(operator=%s, left=%s, right=%s)", className(), operator, left, right));

	return sb.toString();
    }
}
