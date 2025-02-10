package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.Symbol;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTLocal extends ASTNode {
	private final Symbol symbol;
	private int slot;
	private int scopeDepth;

	public ASTLocal(int line, Symbol symbol) {
		super(line);

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
	public void accept(MethodVisitor visitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(PrintVisitor visitor) {
		visitor.visit(this);
	}
}
