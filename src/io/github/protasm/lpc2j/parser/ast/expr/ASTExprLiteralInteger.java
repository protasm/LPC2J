package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTExprLiteralInteger extends ASTExpression {
    private final Integer value;

    public ASTExprLiteralInteger(int line, Token<Integer> token) {
	super(line);

	this.value = token.literal();
    }

    public Integer value() {
	return value;
    }

    @Override
    public LPCType lpcType() {
	return LPCType.LPCINT;
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
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(value=%s)", className(), value));

	return sb.toString();
    }
}
