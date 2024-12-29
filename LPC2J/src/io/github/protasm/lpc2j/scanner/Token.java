package io.github.protasm.lpc2j.scanner;

public class Token {
	private TokenType type;
	private String lexeme;
	private Object literal;
	private int line;

	public Token() {
		this(null, null, null, -1);
	}

	public Token(TokenType type) {
		this(type, null, null, -1);
	}

	public Token(String lexeme) {
		this(null, lexeme, null, -1);
	}

	public Token(TokenType type, String lexeme, Object literal, int line) {
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

	public Object literal() {
		return literal;
	}

	public int line() {
		return line;
	}

	@Override
	public String toString() {
		return type + "," + lexeme + "," + literal + " (" + line + ")";
	}
}
