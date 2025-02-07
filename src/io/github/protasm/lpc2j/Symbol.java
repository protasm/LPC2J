package io.github.protasm.lpc2j;

public class Symbol {
	private LPCType lpcType;
	private String name;

	protected Symbol(LPCType lpcType, String name) {
		this.lpcType = lpcType;
		this.name = name;
	}

	public LPCType lpcType() {
		return lpcType;
	}

	public String name() {
		return name;
	}
}
