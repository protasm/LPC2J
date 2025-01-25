package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTField extends ASTNode {
    private final LPCType lpcType;
    private final String name;
    private final ASTExpression initializer;

    public ASTField(int line, Token<LPCType> typeToken, Token<String> nameToken, ASTExpression initializer) {
	super(line);

	this.lpcType = typeToken.literal();
	this.name = nameToken.lexeme();
	this.initializer = initializer;
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

    @Override
    public String toString() {
	return String.format("%s(type=%s, name=%s, initializer=%s", className, lpcType, name, initializer);
    }
}
