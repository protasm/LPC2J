package io.github.protasm.lpc2j;

public class Local implements HasSymbol {
    private Symbol symbol;
    private int scopeDepth;
    private boolean isCaptured;

    public Local(Symbol symbol) {
	this.symbol = symbol;

	scopeDepth = -1;
	isCaptured = false;
    }

    public int scopeDepth() {
	return scopeDepth;
    }

    public void setScopeDepth(int scopeDepth) {
	this.scopeDepth = scopeDepth;
    }

    public boolean isCaptured() {
	return isCaptured;
    }

    public void setIsCaptured(boolean isCaptured) {
	this.isCaptured = isCaptured;
    }

    @Override
    public String toString() {
	return "[ " + symbol.identifier() + " (" + scopeDepth + ") ]";
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
