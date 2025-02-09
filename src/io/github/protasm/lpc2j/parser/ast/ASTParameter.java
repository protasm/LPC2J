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
	return String.format("%s%s(%s)", ASTNode.indent(), className(), symbol);
    }
}
