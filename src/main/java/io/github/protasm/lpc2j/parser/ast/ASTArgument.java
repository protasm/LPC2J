package io.github.protasm.lpc2j.parser.ast;

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
