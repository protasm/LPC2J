package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class ASTField extends ASTNode {
	private final String ownerName;
	private final LPCType lpcType;
	private final String name;
	private ASTExpression initializer;

	public ASTField(int line, String ownerName, LPCType lpcType, String name) {
		super(line);

		this.ownerName = ownerName;
		this.lpcType = lpcType;
		this.name = name;

		initializer = null;
	}

	public String ownerName() {
		return ownerName;
	}

	public LPCType lpcType() {
		return lpcType;
	}

	public String name() {
		return name;
	}

	public ASTExpression initializer() {
		return initializer;
	}

	public void setInitializer(ASTExpression expr) {
		this.initializer = expr;
	}

	public String descriptor() {
		return lpcType.jType().descriptor();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s(%s %s)\n", className(), lpcType, name));

		return sb.toString();
	}
}
