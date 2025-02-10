package io.github.protasm.lpc2j.parser;

import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;

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
		System.out.println("Where is this called from?");
		return String.format("%sLocal(%s, slot=%d, depth=%d)", PrintVisitor.indentLvl, symbol, slot, scopeDepth);
	}
}
