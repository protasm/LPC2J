package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.type.JType;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.token.Token;

public class Symbol {
    private final String declaredTypeName;
    private LPCType declaredType;
    private LPCType lpcType;
    private final String name;

    public Symbol(LPCType lpcType, String name) {
        this((lpcType != null) ? lpcType.name().toLowerCase() : null, lpcType, name);
    }

    public Symbol(String declaredTypeName, String name) {
        this(declaredTypeName, null, name);
    }

    public Symbol(Token<String> typeToken, Token<String> nameToken) {
        this((typeToken != null) ? typeToken.lexeme() : null, null, nameToken.lexeme());
    }

    private Symbol(String declaredTypeName, LPCType declaredType, String name) {
        this.declaredTypeName = declaredTypeName;
        this.declaredType = declaredType;
        this.lpcType = declaredType;
        this.name = name;
    }

    public LPCType lpcType() {
        return lpcType;
    }

    public LPCType declaredType() {
        return declaredType;
    }

    public String declaredTypeName() {
        return declaredTypeName;
    }

    public void resolveDeclaredType(LPCType resolved) {
        if (resolved == null)
            return;

        this.declaredType = resolved;

        if (this.lpcType == null)
            this.lpcType = resolved;
    }

    public void setLpcType(LPCType lpcType) {
        if (lpcType != null)
            this.lpcType = lpcType;
    }

    public String name() {
        return name;
    }

    public String descriptor() {
        String descriptor = (lpcType == null || lpcType.jType() == null) ? null : lpcType.jType().descriptor();

        return descriptor != null ? descriptor : JType.JOBJECT.descriptor();
    }

    @Override
    public String toString() {
        return String.format("[%s %s]", lpcType, name);
    }
}
