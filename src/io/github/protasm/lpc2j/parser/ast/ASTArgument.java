package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public final class ASTArgument extends ASTNode {
    private final ASTExpression expression;

    public ASTArgument(int line, ASTExpression expr) {
        super(line);

        this.expression = expr;
    }

    public ASTExpression expression() {
        return expression;
    }
}
