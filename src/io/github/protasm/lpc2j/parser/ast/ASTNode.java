package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

public abstract class ASTNode {
    protected static int indentLvl = 0;

    protected final int line;

    public ASTNode(int line) {
	this.line = line;
    }

    public int line() {
	return line;
    }

    protected String className() {
	return getClass().getSimpleName();
    }

    public void toBytecode(MethodVisitor mv) {
	throw new UnsupportedOperationException("toBytecode() not implemented for " + className());
    }
}
