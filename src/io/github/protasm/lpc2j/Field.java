package io.github.protasm.lpc2j;

public class Field implements HasSymbol {
    private Symbol symbol;

    public Field(Symbol symbol) {
	this.symbol = symbol;
    }

    @Override
    public String className() {
	return symbol.className();
    }

    @Override
    public SymbolType sType() {
	return symbol.sType();
    }

    @Override
    public JType jType() {
	return symbol.jType();
    }

    @Override
    public String identifier() {
	return symbol.identifier();
    }

    @Override
    public String descriptor() {
	return symbol.descriptor();
    }
}
