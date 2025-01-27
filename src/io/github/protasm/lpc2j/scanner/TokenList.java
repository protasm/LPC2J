package io.github.protasm.lpc2j.scanner;

import static io.github.protasm.lpc2j.scanner.TokenType.T_EOF;

import java.util.ArrayList;
import java.util.List;

import io.github.protasm.lpc2j.parser.ParseException;

public class TokenList {
    private List<Token<?>> tokens;
    private int currIdx = 0;

    public TokenList() {
	this.tokens = new ArrayList<>();
    }

    public int size() {
	return tokens.size();
    }

    public void add(Token<?> token) {
	tokens.add(token);
    }

    public Token<?> get(int idx) {
	return tokens.get(idx);
    }

    @SuppressWarnings("unchecked")
    public <T> Token<T> get(int idx, Class<T> type) {
	Token<?> token = tokens.get(idx);

	if (type.isInstance(token.literal()))
	    return (Token<T>) token; // Safe cast if token's literal type matches

	throw new IllegalArgumentException("Type mismatch for token at index " + idx);
    }

    @SuppressWarnings("unchecked")
    public <T> Token<T> current() {
	return (Token<T>) tokens.get(currIdx);
    }

    public Token<?> previous() {
	return tokens.get(currIdx - 1);
    }

    public void advance() {
	currIdx++;
    }

    @SuppressWarnings("unchecked")
    public <T> Token<T> consume(TokenType tType, String msg) {
	if (match(tType))
//	    if (tType.clazz().isInstance(previous().tType()))
	    return (Token<T>) previous(); // Safe cast if previous's literal type matches

	throw new ParseException(msg, current());
    }

    public boolean check(TokenType tType) {
	return current().tType() == tType;
    }

    public boolean match(TokenType tType) {
	if (check(tType)) {
	    advance();

	    return true;
	}

	return false;
    }

    public boolean isAtEnd() {
	return currIdx >= tokens.size() || current().tType() == T_EOF;
    }
}
