package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class ASTField extends ASTNode {
    private final String ownerName;
    private final Symbol symbol;
    private ASTExpression initializer;

    public ASTField(int line, String ownerName, Symbol symbol) {
	super(line);

	this.ownerName = ownerName;
	this.symbol = symbol;

	initializer = null;
    }

    public String ownerName() {
	return ownerName;
    }

    public Symbol symbol() {
	return symbol;
    }

    public ASTExpression initializer() {
	return initializer;
    }

    public void setInitializer(ASTExpression expr) {
	this.initializer = expr;
    }

    public String descriptor() {
	return symbol.descriptor();
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(%s)\n", className(), symbol));

	return sb.toString();
    }
}
