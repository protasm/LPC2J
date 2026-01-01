package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;

public final class ASTExprOpUnary extends ASTExpression {
    private final ASTExpression right;
    private final UnaryOpType operator;

    public ASTExprOpUnary(int line, ASTExpression right, UnaryOpType operator) {
        super(line);

        this.right = right;
        this.operator = operator;
    }

    public ASTExpression right() {
        return right;
    }

    public UnaryOpType operator() {
        return operator;
    }

    @Override
    public LPCType lpcType() {
        return operator == UnaryOpType.UOP_NOT ? LPCType.LPCSTATUS : right.lpcType();
    }
}
