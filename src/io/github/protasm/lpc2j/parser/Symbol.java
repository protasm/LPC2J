package io.github.protasm.lpc2j.parser;

import io.github.protasm.lpc2j.scanner.Token;

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
	StringBuilder sb = new StringBuilder();
	
	sb.append(String.format("[%s %s]", lpcType, name));
	
	return sb.toString();
    }
}
