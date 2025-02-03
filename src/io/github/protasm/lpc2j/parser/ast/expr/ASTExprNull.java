package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.LPCType;

public class ASTExprNull extends ASTExpression {
    public ASTExprNull(int line) {
	super(line);
    }

    @Override
    public LPCType lpcType() {
	return LPCType.LPCNULL;
    }
}
