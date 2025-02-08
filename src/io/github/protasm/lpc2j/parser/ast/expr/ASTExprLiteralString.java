package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTExprLiteralString extends ASTExpression {
	private final String value;

	public ASTExprLiteralString(int line, Token<String> token) {
		super(line);

		this.value = token.literal();
	}

	public String value() {
		return value;
	}

	@Override
	public LPCType lpcType() {
		return LPCType.LPCSTRING;
	}

	@Override
	public void toBytecode(MethodVisitor mv) {
		mv.visitLdcInsn(value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s(value=%s)", className(), value));

		return sb.toString();
	}
}
