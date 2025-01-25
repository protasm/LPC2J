package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import common.LTType;
import scanner.Token;

public class ASTExprVariable extends ASTExpression {
    private final String name;

    public ASTExprVariable(Token startToken) {
	super(startToken);

	this.name = startToken.lexeme();
    }

    public String name() {
	return name;
    }

    @Override
    public LTType type() {
	return LTType.LT_NULL; // foo
    }

    @Override
    public void toBytecode(MethodVisitor mv) {

    }

    @Override
    public String toString() {
	return String.format("ASTExprVariable(name=%s)", name);
    }
}
