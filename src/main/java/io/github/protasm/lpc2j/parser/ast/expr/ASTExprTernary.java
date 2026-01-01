package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprTernary extends ASTExpression {
    private LPCType lpcType;
    private final ASTExpression condition;
    private final ASTExpression thenBranch;
    private final ASTExpression elseBranch;

    public ASTExprTernary(int line, ASTExpression condition, ASTExpression thenBranch, ASTExpression elseBranch) {
        super(line);

        this.lpcType = null;
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public ASTExpression condition() {
        return condition;
    }

    public ASTExpression thenBranch() {
        return thenBranch;
    }

    public ASTExpression elseBranch() {
        return elseBranch;
    }

    @Override
    public LPCType lpcType() {
        return lpcType != null ? lpcType : LPCType.LPCMIXED;
    }

    public void setLpcType(LPCType lpcType) {
        this.lpcType = lpcType;
    }
}
