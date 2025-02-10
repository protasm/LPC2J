package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
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
	public void accept(MethodVisitor mv) {
		mv.visitLdcInsn(value);
	}

	@Override
	public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(PrintVisitor visitor) {
		visitor.visit(this);
	}
}
