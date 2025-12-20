package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprLocalAccess extends ASTExpression {
    private final ASTLocal local;

    public ASTExprLocalAccess(int line, ASTLocal local) {
        super(line);

        this.local = local;
    }

    public ASTLocal local() {
        return local;
    }

    @Override
    public LPCType lpcType() {
        return local.symbol().lpcType();
    }
}
