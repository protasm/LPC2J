package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

public abstract class ASTNode {
    protected final int line;
    protected final String className;

    public ASTNode(int line) {
	this.line = line;

	this.className = this.getClass().getSimpleName();
    }
    
    public int line() {
	return line;
    }
    
    public void toBytecode(MethodVisitor mv) {
	throw new UnsupportedOperationException("toBytecode() not implemented for " + this.getClass().getSimpleName());
    }

    @Override
    public String toString() {
	return className;
    }
}
