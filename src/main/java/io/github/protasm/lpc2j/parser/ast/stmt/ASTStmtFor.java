package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;

public final class ASTStmtFor extends ASTStatement {
    private final ASTExpression initializer;
    private final ASTExpression condition;
    private final ASTExpression update;
    private final ASTStatement body;

    public ASTStmtFor(
            int line, ASTExpression initializer, ASTExpression condition, ASTExpression update, ASTStatement body) {
        super(line);
        this.initializer = initializer;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public ASTExpression initializer() {
        return initializer;
    }

    public ASTExpression condition() {
        return condition;
    }

    public ASTExpression update() {
        return update;
    }

    public ASTStatement body() {
        return body;
    }
}
