package io.github.protasm.lpc2j.parser;

public class Local {
    private final Symbol symbol;
    private int slot;
    private int scopeDepth;

    public Local(Symbol symbol) {
	this.symbol = symbol;

	slot = -1;
	scopeDepth = -1;
    }

    public Symbol symbol() {
	return symbol;
    }

    public int slot() {
	return slot;
    }

    public int scopeDepth() {
	return scopeDepth;
    }

    public void setSlot(int slot) {
	this.slot = slot;
    }

    public void setScopeDepth(int scopeDepth) {
	this.scopeDepth = scopeDepth;
    }

    public String descriptor() {
	return symbol.descriptor();
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("Local(symbol=%s, slot=%d, depth=%d)", symbol, slot, scopeDepth));

	return sb.toString();
    }
}
