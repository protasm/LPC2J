package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.BinaryOpType;
import io.github.protasm.lpc2j.LPCType;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;

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
			case BOP_LT:
				Label trueLabel = new Label();
				Label endLabel = new Label();

				// Compare left < right
				mv.visitJumpInsn(IF_ICMPLT, trueLabel); // Jump to trueLabel if left < right

				// False case: Push 0 (false)
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);

				// True case: Push 1 (true)
				mv.visitLabel(trueLabel);
				mv.visitInsn(ICONST_1);

				// End label
				mv.visitLabel(endLabel);
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
