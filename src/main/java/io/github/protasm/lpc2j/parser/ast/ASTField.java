package io.github.protasm.lpc2j.parser.ast;

public final class ASTField extends ASTNode {
    private final String ownerName;
    private final Symbol symbol;
    private ASTExpression initializer;
    private final boolean declared;
    private boolean defined;

    public ASTField(int line, String ownerName, Symbol symbol) {
        this(line, ownerName, symbol, true);
    }

    public ASTField(int line, String ownerName, Symbol symbol, boolean declared) {
        super(line);

        this.ownerName = ownerName;
        this.symbol = symbol;
        this.declared = declared;

        initializer = null;
        defined = false;
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

    public boolean isDeclared() {
        return declared;
    }

    public boolean isDefined() {
        return defined;
    }

    public void markDefined() {
        this.defined = true;
    }
}
