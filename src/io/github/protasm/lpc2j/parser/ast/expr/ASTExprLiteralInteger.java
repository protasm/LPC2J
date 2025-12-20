package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.token.Token;

public final class ASTExprLiteralInteger extends ASTExpression {
    private final Integer value;

    public ASTExprLiteralInteger(int line, Token<Integer> token) {
        super(line);

        this.value = token.literal();
    }

    public Integer value() {
        return value;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCINT;
    }
}
