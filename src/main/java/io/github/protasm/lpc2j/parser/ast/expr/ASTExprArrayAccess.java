package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.Objects;

public final class ASTExprArrayAccess extends ASTExpression {
    private final ASTExpression target;
    private final ASTExpression index;

    public ASTExprArrayAccess(int line, ASTExpression target, ASTExpression index) {
        super(line);
        this.target = Objects.requireNonNull(target, "target");
        this.index = Objects.requireNonNull(index, "index");
    }

    public ASTExpression target() {
        return target;
    }

    public ASTExpression index() {
        return index;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
