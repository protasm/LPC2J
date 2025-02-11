package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.ast.visitor.BytecodeVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

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

    public abstract void accept(BytecodeVisitor visitor);
    public abstract void accept(TypeInferenceVisitor visitor, LPCType lpcType);
    public abstract void accept(PrintVisitor visitor);
}
