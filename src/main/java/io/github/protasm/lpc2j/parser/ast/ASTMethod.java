package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;

public final class ASTMethod extends ASTNode {
    private final String ownerName;
    private final Symbol symbol;
    private ASTParameters parameters;
    private ASTStmtBlock body;
    private final java.util.List<ASTLocal> locals;
    private ASTMethod overrides;
    private final boolean declared;
    private boolean defined;

    public ASTMethod(int line, String ownerName, Symbol symbol) {
        this(line, ownerName, symbol, true);
    }

    public ASTMethod(int line, String ownerName, Symbol symbol, boolean declared) {
        super(line);

        this.ownerName = ownerName;
        this.symbol = symbol;
        this.declared = declared;

        parameters = null;
        body = null;
        locals = new java.util.ArrayList<>();
        defined = false;
    }

    public String ownerName() {
        return ownerName;
    }

    public Symbol symbol() {
        return symbol;
    }

    public ASTParameters parameters() {
        return parameters;
    }

    public void setParameters(ASTParameters parameters) {
        this.parameters = parameters;
    }

    public ASTStmtBlock body() {
        return body;
    }

    public void setBody(ASTStmtBlock body) {
        this.body = body;
    }

    public java.util.List<ASTLocal> locals() {
        return locals;
    }

    public void addLocal(ASTLocal local) {
        locals.add(local);
    }

    public ASTMethod overrides() {
        return overrides;
    }

    public void setOverrides(ASTMethod overrides) {
        this.overrides = overrides;
    }

    public String descriptor() {
        return parameters.descriptor() + symbol.descriptor();
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
