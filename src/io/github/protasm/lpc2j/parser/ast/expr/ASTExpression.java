package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

public abstract class ASTExpression extends ASTNode {
    public ASTExpression(int line) {
	super(line);
    }

    public abstract LPCType lpcType();
    public abstract void typeInference(LPCType lpcType);
}
