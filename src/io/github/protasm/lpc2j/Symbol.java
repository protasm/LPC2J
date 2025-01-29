package io.github.protasm.lpc2j;

public class Symbol {
	private ClassBuilder cb;
	private SymbolType sType;
	private JType jType;
	private String identifier;
	private String descriptor;

	protected Symbol(ClassBuilder cb, SymbolType sType, JType jType, String identifier, String descriptor) {
		this.cb = cb;
		this.sType = sType;
		this.jType = jType;
		this.identifier = identifier;
		this.descriptor = descriptor;
	}

	public ClassBuilder cb() {
		return cb;
	}

	public String className() {
		return cb.className();
	}

	public SymbolType sType() {
		return sType;
	}

	public JType jType() {
		return jType;
	}

	public String identifier() {
		return identifier;
	}

	public String descriptor() {
		return descriptor;
	}
}
