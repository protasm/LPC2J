package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.token.Token;

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
	public void accept(Compiler visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
		visitor.visit(this, lpcType);
	}

	@Override
	public void accept(PrintVisitor visitor) {
		visitor.visit(this);
	}
}
