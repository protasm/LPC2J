package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTExprVariable extends ASTExpression {
    private final String name;

    public ASTExprVariable(int line, Token<String> nameToken) {
	super(line);

	this.name = nameToken.lexeme();
    }

    public String name() {
	return name;
    }

    @Override
    public LPCType lpcType() {
	return LPCType.LPCNULL; // temp
    }

    @Override
    public void toBytecode(MethodVisitor mv) {

    }

    @Override
    public String toString() {
	return String.format("%s(name=%s)", className, name);
    }
}
