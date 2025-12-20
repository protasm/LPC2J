package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.ast.visitor.ASTVisitor;

public abstract sealed class ASTNode
        permits ASTArgument,
                ASTExpression,
                ASTField,
                ASTListNode,
                ASTLocal,
                ASTMapNode,
                ASTMethod,
                ASTObject,
                ASTParameter,
                ASTStatement {
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

    public final void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
