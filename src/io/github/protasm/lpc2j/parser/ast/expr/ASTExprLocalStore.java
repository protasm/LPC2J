package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprLocalStore extends ASTExpression {
    private final ASTLocal local;
    private final ASTExpression value;

    public ASTExprLocalStore(int line, ASTLocal local, ASTExpression value) {
        super(line);

        this.local = local;
        this.value = value;
    }

    public ASTLocal local() {
        return local;
    }

    public ASTExpression value() {
        return value;
    }

    @Override
    public LPCType lpcType() {
        return local.symbol().lpcType();
    }
}
