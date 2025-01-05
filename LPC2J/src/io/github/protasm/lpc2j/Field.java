package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.scanner.Token;

public class Field extends Symbol {
    public Field(JType type, String name) {
	super(type, name);
    }

    public Field(Token typeToken, Token nameToken) {
	super(typeToken, nameToken);
    }
}
