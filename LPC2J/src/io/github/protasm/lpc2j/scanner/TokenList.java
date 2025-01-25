package io.github.protasm.lpc2j.scanner;

import static io.github.protasm.lpc2j.scanner.TokenType.T_EOF;

import java.util.ArrayList;
import java.util.List;

import io.github.protasm.lpc2j.parser.ParseException;

public class TokenList {
    private List<Token<? extends Object>> tokens;
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

    public <T> Token<T> get(int idx, Class<T> type) {
	Token<?> token = tokens.get(idx);

	if (type.isInstance(token.literal()))
	    return (Token<T>) token; // Safe cast if type matches

	throw new IllegalArgumentException("Type mismatch for token at index " + idx);
    }

    public Token<?> current() {
	return tokens.get(currIdx);
    }

    public Token<?> previous() {
	return tokens.get(currIdx - 1);
    }

    public void advance() {
	currIdx++;
    }

    public Token<?> consume(TokenType type, String msg) {
	if (match(type))
	    return previous();

	throw new ParseException(msg, current());
    }

    public <T> Token<T> consume(TokenType type, Class<T> clazz, String msg) {
	if (match(type))
	    if (clazz.isInstance(previous().type()))
		return (Token<T>) previous(); // Safe cast if type matches

	throw new ParseException(msg, current());
    }

    public Token<?> consume(TokenType[] types, String msg) {
	for (TokenType type : types)
	    if (match(type))
		return previous();

	throw new ParseException(msg, current());
    }

    public boolean check(TokenType type) {
	return current().type() == type;
    }

    public boolean match(TokenType type) {
	if (check(type)) {
	    advance();

	    return true;
	}

	return false;
    }

    public boolean isAtEnd() {
	return currIdx >= tokens.size() || current().type() == T_EOF;
    }
}
