package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import common.LTType;
import scanner.Token;

public class ASTExprIntegerLiteral extends ASTExpression {
    private final Integer value;

    public ASTExprIntegerLiteral(Token startToken) {
	super(startToken);

	this.value = (Integer) startToken.literal();
    }

    public Integer value() {
	return value;
    }

    @Override
    public LTType type() {
	return LTType.LT_INT;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	if (value >= -1 && value <= 5)
	    mv.visitInsn(Opcodes.ICONST_0 + value);
	else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
	    mv.visitIntInsn(Opcodes.BIPUSH, value);
	else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
	    mv.visitIntInsn(Opcodes.SIPUSH, value);
	else
	    mv.visitLdcInsn(value);
    }

    @Override
    public String toString() {
	return String.format("ASTExprIntegerLiteral(value=%s)", value);
    }
}
