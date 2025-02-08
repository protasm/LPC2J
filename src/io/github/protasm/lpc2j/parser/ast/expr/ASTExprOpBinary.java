package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.BinaryOpType;
import io.github.protasm.lpc2j.parser.LPCType;

import static io.github.protasm.lpc2j.parser.LPCType.*;
import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;

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
	return left.lpcType() == lType && right.lpcType() == rType;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	left.toBytecode(mv);
	right.toBytecode(mv);

	switch (operator) {
	case BOP_ADD:
	case BOP_SUB:
	case BOP_MULT:
	case BOP_DIV:
	    mv.visitInsn(operator.opcode());
	    break;
	case BOP_GT:
	case BOP_GE:
	case BOP_LT:
	case BOP_LE:
	case BOP_EQ:
	    Label labelTrue = new Label();
	    Label labelEnd = new Label();

	    // Compare left vs right
	    mv.visitJumpInsn(operator.opcode(), labelTrue);

	    // False case: Push 0 (false)
	    mv.visitInsn(ICONST_0);
	    mv.visitJumpInsn(GOTO, labelEnd);

	    // True case: Push 1 (true)
	    mv.visitLabel(labelTrue);
	    mv.visitInsn(ICONST_1);

	    // End label
	    mv.visitLabel(labelEnd);
	    break;
	default:
	    throw new UnsupportedOperationException("Unsupported operator: " + operator);
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(%s)", className(), operator));

	return sb.toString();
    }
}
