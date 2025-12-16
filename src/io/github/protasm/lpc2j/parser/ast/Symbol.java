package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.token.Token;

public class Symbol {
    private final LPCType declaredType;
    private LPCType lpcType;
    private final String name;

    public Symbol(LPCType lpcType, String name) {
        this.declaredType = lpcType;
        this.lpcType = lpcType;
        this.name = name;
    }

    public Symbol(Token<LPCType> typeToken, Token<String> nameToken) {
        declaredType = typeToken.literal();
        lpcType = declaredType;
        name = nameToken.lexeme();
    }

    public LPCType lpcType() {
        return lpcType;
    }

    public LPCType declaredType() {
        return declaredType;
    }

    public void setLpcType(LPCType lpcType) {
        if (lpcType != null)
            this.lpcType = lpcType;
    }

    public String name() {
        return name;
    }

    public String descriptor() {
        return lpcType.jType().descriptor();
    }

    @Override
    public String toString() {
        return String.format("[%s %s]", lpcType, name);
    }
}
