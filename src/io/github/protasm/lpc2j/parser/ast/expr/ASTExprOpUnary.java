package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.UnaryOpType;

import static org.objectweb.asm.Opcodes.*;
import static io.github.protasm.lpc2j.LPCType.*;

public class ASTExprOpUnary extends ASTExpression {
    private final ASTExpression right;
    private final UnaryOpType operator;

    public ASTExprOpUnary(int line, ASTExpression right, UnaryOpType operator) {
	super(line);

	this.right = right;
	this.operator = operator;
    }

    public ASTExpression right() {
	return right;
    }

    public UnaryOpType operator() {
	return operator;
    }

    @Override
    public LPCType lpcType() {
	switch (operator) {
	case UOP_NEGATE:
	    if (right.lpcType() == LPCINT)
		return LPCINT;
	    else
		throw new IllegalStateException("Unary '-' operator requires an integer operand.");
	case UOP_NOT:
	    if (right.lpcType() == LPCSTATUS)
		return LPCSTATUS;
	    else
		throw new IllegalStateException("Logical '!' operator requires a boolean operand.");
	default:
	    throw new UnsupportedOperationException("Unsupported operator: " + operator);
	}
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	right.toBytecode(mv);

	switch (operator) {
	case UOP_NEGATE: // Unary minus (-)
	    mv.visitInsn(INEG);
	    break;
	case UOP_NOT: // Logical NOT (!)
	    Label trueLabel = new Label();
	    Label endLabel = new Label();

	    // Jump if operand is false (0)
	    mv.visitJumpInsn(IFEQ, trueLabel);

	    // Operand is true, push 0 (false)
	    mv.visitInsn(ICONST_0);
	    mv.visitJumpInsn(GOTO, endLabel);

	    // Operand is false, push 1 (true)
	    mv.visitLabel(trueLabel);
	    mv.visitInsn(ICONST_1);

	    // End
	    mv.visitLabel(endLabel);

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
