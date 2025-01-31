/**
 *
 */
package io.github.protasm.lpc2j;

import java.util.List;

import io.github.protasm.lpc2j.scanner.Token;

/**
 *
 */
public class FieldInitializer {
    private Token typeToken;
    private Token nameToken;
    private List<Token> initTokens;

    public FieldInitializer(Token typeToken, Token nameToken, List<Token> initTokens) {
	this.typeToken = typeToken;
	this.nameToken = nameToken;
	this.initTokens = initTokens;
    }

    public Token typeToken() {
	return typeToken;
    }

    public Token nameToken() {
	return nameToken;
    }

    public List<Token> initTokens() {
	return initTokens;
    }
}
