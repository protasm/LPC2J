package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.scanner.Token;

public abstract class ASTNode {
    protected final int line;

    public ASTNode(Token<?> startToken) {
	this.line = startToken.line();
    }

    public int line() {
	return line;
    }

    public void toBytecode(MethodVisitor mv) {
	throw new UnsupportedOperationException("toBytecode() not implemented for " + this.getClass().getSimpleName());
    }

    @Override
    public String toString() {
	return String.format("%s", this.getClass().getSimpleName());
    }
}
