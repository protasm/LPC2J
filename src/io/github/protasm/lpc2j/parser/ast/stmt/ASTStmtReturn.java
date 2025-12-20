package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public final class ASTStmtReturn extends ASTStatement {
    private final ASTExpression returnValue;

    public ASTStmtReturn(int line, ASTExpression returnValue) {
        super(line);
        this.returnValue = returnValue;
    }

    public ASTExpression returnValue() {
        return returnValue;
    }
}
