package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

public abstract class ASTNode {
    public static int indentLvl = 0;

    protected final int line;

    public ASTNode(int line) {
	this.line = line;
    }

    public int line() {
	return line;
    }

    public static String indent() {
	return indent("\u22A2");
    }
    
    public static String indent(String suffix) {
	return "   ".repeat(indentLvl) + suffix;
    }

    protected String className() {
	return getClass().getSimpleName();
    }

    public abstract void accept(MethodVisitor mv);
    public abstract void accept(ASTNodeVisitor nv);
}
