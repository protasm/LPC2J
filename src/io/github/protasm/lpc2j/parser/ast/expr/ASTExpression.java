package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTNode;
import io.github.protasm.lpc2j.parser.type.LPCType;

public abstract class ASTExpression extends ASTNode {
    public ASTExpression(int line) {
	super(line);
    }

    public abstract LPCType lpcType();
}
