package io.github.protasm.lpc2j.parser.ast.expr;

import common.LTType;
import scanner.Token;

public class ASTExprNull extends ASTExpression {
    public ASTExprNull(Token startToken) {
	super(startToken);
    }

    @Override
    public LTType type() {
	return LTType.LT_NULL;
    }

    @Override
    public String toString() {
	return "ASTExprNull";
    }
}
