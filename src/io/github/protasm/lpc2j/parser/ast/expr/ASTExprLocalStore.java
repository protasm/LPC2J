package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.Local;
import io.github.protasm.lpc2j.parser.ast.ASTField;

import static org.objectweb.asm.Opcodes.*;

public class ASTExprLocalStore extends ASTExpression {
	private Local local;
	private ASTExpression value;

	public ASTExprLocalStore(int line, Local local, ASTExpression value) {
		super(line);

		this.local = local;
		this.value = value;
	}

	public Local local() {
		return local;
	}

	public ASTExpression value() {
		return value;
	}

	@Override
	public LPCType lpcType() {
		return local.lpcType();
	}

	@Override
	public void toBytecode(MethodVisitor mv) {
		value.toBytecode(mv);

		switch (local.lpcType()) {
			case LPCINT:
			case LPCSTATUS:
				mv.visitVarInsn(ISTORE, local.slot());
				break;
			case LPCSTRING:
			case LPCOBJECT:
				mv.visitVarInsn(ASTORE, local.slot());
				break;
			default:
				throw new IllegalStateException("Unsupported type: " + local.lpcType());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s\n", className()));

		return sb.toString();
	}
}
