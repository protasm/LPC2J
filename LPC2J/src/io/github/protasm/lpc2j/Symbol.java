package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.scanner.Token;

public abstract class Symbol {
    protected JType jType;
    protected String name;

    protected Symbol(JType type, String name) {
	this.jType = type;
	this.name = name;
    }

    protected Symbol(Token typeToken, Token nameToken) {
	String lpcType = typeToken.lexeme();

	this.jType = JType.jTypeForLPCType(lpcType);

	this.name = nameToken.lexeme();

    }

    public JType jType() {
	return jType;
    }

    public String name() {
	return name;
    }

    public String desc() {
	return jType.descriptor();
    }
}
