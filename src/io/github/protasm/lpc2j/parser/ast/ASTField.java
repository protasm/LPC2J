package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTField extends ASTNode {
    private final String ownerName;
    private final LPCType lpcType;
    private final String name;
    private final ASTExpression initializer;

    public ASTField(int line, String ownerName, Token<LPCType> typeToken, Token<String> nameToken,
	    ASTExpression initializer) {
	super(line);

	this.ownerName = ownerName;
	this.lpcType = typeToken.literal();
	this.name = nameToken.lexeme();
	this.initializer = initializer;
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

    public String descriptor() {
	return lpcType.jType().descriptor();
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	if (initializer != null)
	    sb.append(String.format("%s(type=%s, name=%s, initializer=%s)\n", className(), lpcType, name, initializer));
	else
	    sb.append(String.format("%s(type=%s, name=%s)\n", className(), lpcType, name));

	return sb.toString();
    }
}
