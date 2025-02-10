package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;

public abstract class ASTNode {
	protected final int line;

	public ASTNode(int line) {
		this.line = line;
	}

	public int line() {
		return line;
	}

	public String className() {
		return getClass().getSimpleName();
	}

	public abstract void accept(MethodVisitor visitor);
	public abstract void accept(TypeInferenceVisitor visitor, LPCType lpcType);
	public abstract void accept(PrintVisitor visitor);
}
