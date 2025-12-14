package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.token.Token;

public class Symbol {
    private final LPCType lpcType;
    private final String name;

    public Symbol(LPCType lpcType, String name) {
        this.lpcType = lpcType;
        this.name = name;
    }

    public Symbol(Token<LPCType> typeToken, Token<String> nameToken) {
        lpcType = typeToken.literal();
        name = nameToken.lexeme();
    }

    public LPCType lpcType() {
        return lpcType;
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
