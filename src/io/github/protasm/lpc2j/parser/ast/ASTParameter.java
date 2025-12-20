package io.github.protasm.lpc2j.parser.ast;

public final class ASTParameter extends ASTNode {
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
}
