package io.github.protasm.lpc2j.parser.ast.expr;

import ast.ASTNode;
import common.LTType;
import scanner.Token;

public abstract class ASTExpression extends ASTNode {
    public ASTExpression(Token startToken) {
	super(startToken);
    }

    public abstract LTType type();

    @Override
    public abstract String toString();
}
