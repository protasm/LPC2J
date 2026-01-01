package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.type.LPCType;

public abstract non-sealed class ASTExpression extends ASTNode {
    public ASTExpression(int line) {
        super(line);
    }

    public abstract LPCType lpcType();
}
