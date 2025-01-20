package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.JType;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTField extends ASTNode {
    private final JType type;
    private final String name;
    private final ASTExpression initializer;

    public ASTField(Token<LPCType> typeToken, Token<String> nameToken, ASTExpression initializer) {
	super(typeToken);

	this.type = typeToken.literal().toJType();
	this.name = nameToken.lexeme();
	this.initializer = initializer;
    }

    public JType type() {
	return type;
    }

    public String name() {
	return name;
    }

    public ASTExpression initializer() {
	return initializer;
    }

    @Override
    public String toString() {
	return String.format("ASTField(type=%s, name=%s, initializer=%s", type, name, initializer);
    }
}
