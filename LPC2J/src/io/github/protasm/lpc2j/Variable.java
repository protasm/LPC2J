package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.scanner.Token;

public abstract class Variable {
    private JType jType;
    private String name;

    public Variable(JType type, String name) {
	this.jType = type;
	this.name = name;
    }

    public Variable(Token typeToken, Token nameToken) {
	String lpcType = typeToken.lexeme();

	this.jType = JType.jTypeForLPCType(lpcType);;
	this.name = nameToken.lexeme();;
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
