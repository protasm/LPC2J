package io.github.protasm.lpc2j.parser.ast;

import common.LTType;
import scanner.Token;

public class ASTParameter extends ASTNode {
    private final LTType type;
    private final String name;

    public ASTParameter(Token startToken, Token nameToken) {
	super(startToken);

	this.type = startToken.type().toLTType();
	this.name = nameToken.lexeme();
    }

    public LTType type() {
	return type;
    }

    public String name() {
	return name;
    }

    @Override
    public String toString() {
	return String.format("ASTParameter(type=%s, name=%s)", type, name);
    }
}
