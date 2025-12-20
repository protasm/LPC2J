package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprLiteralTrue extends ASTExpression {
    public ASTExprLiteralTrue(int line) {
        super(line);
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCSTATUS;
    }
}
