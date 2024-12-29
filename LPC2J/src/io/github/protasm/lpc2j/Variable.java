package io.github.protasm.lpc2j;

public class Variable {
	private J_Type type;
	private String name;

	public Variable(J_Type type, String name) {
		this.type = type;
		this.name = name;
	}

	public J_Type type() {
		return type;
	}

	public String name() {
		return name;
	}

	public String desc() {
		return type.descriptor();
	}
}
