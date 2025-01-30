package io.github.protasm.lpc2j.scanner;

public class Token<T> {
	private final TokenType tType;
	private final String lexeme;
	private final T literal;
	private final int line;

	public Token() {
		this(null, null, null, -1);
	}

	public Token(TokenType tType) {
		this(tType, null, null, -1);
	}

	public Token(String lexeme) {
		this(null, lexeme, null, -1);
	}

	public Token(TokenType tType, String lexeme, T literal, int line) {
		this.tType = tType;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line;
	}

	public TokenType tType() {
		return tType;
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

	@SuppressWarnings("unchecked")
	public Class<T> literalType() {
		return (Class<T>) literal.getClass();
	}

	public int line() {
		return line;
	}

	@Override
	public String toString() {
		if (tType == TokenType.T_EOF)
			return tType.toString();
		if (literal == null)
			return tType + "(" + lexeme + "), ";
		else
			return tType + "<" + literal.getClass().getSimpleName() + ">(" + lexeme + "), ";
	}
}
