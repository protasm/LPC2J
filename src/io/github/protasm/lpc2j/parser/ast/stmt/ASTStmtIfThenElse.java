package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public final class ASTStmtIfThenElse extends ASTStatement {
    private final ASTExpression condition;
    private final ASTStatement thenBranch;
    private final ASTStatement elseBranch;

    public ASTStmtIfThenElse(int line, ASTExpression condition, ASTStatement thenBranch, ASTStatement elseBranch) {
        super(line);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public ASTExpression condition() {
        return condition;
    }

    public ASTStatement thenBranch() {
        return thenBranch;
    }

    public ASTStatement elseBranch() {
        return elseBranch;
    }
}
