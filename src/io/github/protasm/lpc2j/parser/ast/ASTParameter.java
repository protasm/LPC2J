package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.Symbol;

public class ASTParameter extends ASTNode {
    private final Symbol symbol;

    public ASTParameter(int line, Symbol symbol) {
	super(line);

	this.symbol = symbol;
    }

    public Symbol symbol() {
	return symbol;
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
