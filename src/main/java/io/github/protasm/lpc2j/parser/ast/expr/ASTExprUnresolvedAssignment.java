package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.type.AssignOpType;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprUnresolvedAssignment extends ASTExpression {
    private final String name;
    private final AssignOpType operator;
    private final ASTExpression value;

    public ASTExprUnresolvedAssignment(int line, String name, AssignOpType operator, ASTExpression value) {
        super(line);

        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public AssignOpType operator() {
        return operator;
    }

    public ASTExpression value() {
        return value;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
