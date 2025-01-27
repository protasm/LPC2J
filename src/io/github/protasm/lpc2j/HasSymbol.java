package io.github.protasm.lpc2j;

public interface HasSymbol {
    public abstract String className();

    public abstract SymbolType sType();

    public abstract JType jType();

    public abstract String identifier();

    public abstract String descriptor();
}
