package io.github.protasm.lpc2j.parser.ast;

public final class ASTLocal extends ASTNode {
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
}
