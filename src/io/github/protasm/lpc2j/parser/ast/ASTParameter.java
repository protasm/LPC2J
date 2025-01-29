package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTParameter extends ASTNode {
    private final LPCType type;
    private final String name;

    public ASTParameter(int line, Token<LPCType> typeToken, Token<String> nameToken) {
	super(line);

	this.type = typeToken.literal();
	this.name = nameToken.lexeme();
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

	sb.append(String.format("%s(type=%s, name=%s)\n", className(), type, name));

	return sb.toString();
    }
}
