package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.LPCType;

public class ASTExprLiteralTrue extends ASTExpression {
    public ASTExprLiteralTrue(int line) {
	super(line);
    }

    @Override
    public LPCType lpcType() {
	return LPCType.LPCSTATUS;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	mv.visitInsn(Opcodes.ICONST_1);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s", className()));

	return sb.toString();
    }
}
