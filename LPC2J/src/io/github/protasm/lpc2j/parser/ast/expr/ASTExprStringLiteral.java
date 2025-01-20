package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import common.LTType;
import scanner.Token;

public class ASTExprStringLiteral extends ASTExpression {
    private final String value;

    public ASTExprStringLiteral(Token startToken) {
	super(startToken);

	this.value = startToken.lexeme();
    }

    public String value() {
	return value;
    }

    @Override
    public LTType type() {
	return LTType.LT_STRING;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	mv.visitLdcInsn(value);
    }

    @Override
    public String toString() {
	return String.format("ASTExprStringLiteral(value=%s)", value);
    }
}
