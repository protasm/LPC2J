package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.LPCType;

public class ASTExprLiteralFalse extends ASTExpression {
	public ASTExprLiteralFalse(int line) {
		super(line);
	}

	@Override
	public LPCType lpcType() {
		return LPCType.LPCSTATUS;
	}

	@Override
	public void toBytecode(MethodVisitor mv) {
		mv.visitInsn(Opcodes.ICONST_0);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s(value=%s)", className()));

		return sb.toString();
	}
}
