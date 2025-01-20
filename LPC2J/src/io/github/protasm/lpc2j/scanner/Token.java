package io.github.protasm.lpc2j.scanner;

public class Token<T> {
    private final TokenType type;
    private final String lexeme;
    private final T literal;
    private final int line;

    public Token() {
	this(null, null, null, -1);
    }

    public Token(TokenType type) {
	this(type, null, null, -1);
    }

    public Token(String lexeme) {
	this(null, lexeme, null, -1);
    }

    public Token(TokenType type, String lexeme, T literal, int line) {
	this.type = type;
	this.lexeme = lexeme;
	this.literal = literal;
	this.line = line;
    }

    public TokenType type() {
	return type;
    }

    public String lexeme() {
	return lexeme;
    }

    public int length() {
	return lexeme.length();
    }

    public T literal() {
	return literal;
    }

    public int line() {
	return line;
    }

    @Override
    public String toString() {
	if (literal == null)
		return type + " " + lexeme + ", ";
	else
	return type + "<" + literal.getClass().getSimpleName() + "> " + lexeme + ", ";
    }
}
