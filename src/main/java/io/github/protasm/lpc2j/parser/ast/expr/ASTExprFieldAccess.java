package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprFieldAccess extends ASTExpression {
    private final ASTField field;

    public ASTExprFieldAccess(int line, ASTField field) {
        super(line);

        this.field = field;
    }

    public ASTField field() {
        return field;
    }

    @Override
    public LPCType lpcType() {
        return field.symbol().lpcType();
    }
}
