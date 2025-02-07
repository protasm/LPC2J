package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.LPCType;

public class ASTParameter extends ASTNode {
	private final LPCType type;
	private final String name;

	public ASTParameter(int line, LPCType lpcType, String name) {
		super(line);

		this.type = lpcType;
		this.name = name;
	}

	public LPCType type() {
		return type;
	}

	public String name() {
		return name;
	}

	public String descriptor() {
		return type.jType().descriptor();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s(%s %s)\n", className(), type, name));

		return sb.toString();
	}
}
