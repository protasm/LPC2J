package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprOpBinary extends ASTExpression {
    private final ASTExpression left;
    private final ASTExpression right;
    private final BinaryOpType operator;

    public ASTExprOpBinary(int line, ASTExpression left, ASTExpression right, BinaryOpType operator) {
        super(line);

        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public ASTExpression left() {
        return left;
    }

    public ASTExpression right() {
        return right;
    }

    public BinaryOpType operator() {
        return operator;
    }

    @Override
    public LPCType lpcType() {
        return switch (operator) {
        case BOP_ADD -> (left.lpcType() == LPCType.LPCSTRING || right.lpcType() == LPCType.LPCSTRING)
                ? LPCType.LPCSTRING
                : LPCType.LPCINT;
        case BOP_SUB, BOP_MULT, BOP_DIV -> LPCType.LPCINT;
        case BOP_GT, BOP_GE, BOP_LT, BOP_LE, BOP_EQ, BOP_NE, BOP_OR, BOP_AND -> LPCType.LPCSTATUS;
        };
    }
}
