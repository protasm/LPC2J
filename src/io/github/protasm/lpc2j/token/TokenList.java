package io.github.protasm.lpc2j.token;

import static io.github.protasm.lpc2j.token.TokenType.T_EOF;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import io.github.protasm.lpc2j.parser.ParseException;

public class TokenList {
	private final List<Token<?>> tokens;
	private int currIdx = 0;

	public TokenList() {
		this.tokens = new ArrayList<>();
	}

	public void reset() {
		currIdx = 0;
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

        public Token<?> peek(int offset) {
                if (tokens.isEmpty())
                        throw new IllegalStateException("Cannot peek an empty token list.");

                int idx = currIdx + offset;

                if (idx < 0)
                        idx = 0;

                if (idx >= tokens.size())
                        idx = tokens.size() - 1;

                return tokens.get(idx);
        }

	@SuppressWarnings("unchecked")
	public <T> Token<T> previous() {
		return (Token<T>) tokens.get(currIdx - 1);
	}

	public void advance() {
		currIdx++;
	}

	public void advanceThrough(TokenType tType) {
		while (!match(tType)) {
			advance();

			if (check(T_EOF))
				throw new ParseException("Expected " + tType + ".");
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Token<T> consume(TokenType tType, String msg) {
		if (match(tType))
//	    if (tType.clazz().isInstance(previous().tType()))
			return (Token<T>) previous(); // Safe cast if previous's literal type matches

		throw new ParseException(msg, current());
	}

	public boolean check(TokenType tType) {
		return current().type() == tType;
	}

	public boolean match(TokenType tType) {
		if (check(tType)) {
			advance();

			return true;
		}

		return false;
	}

	public boolean isAtEnd() {
		return (currIdx >= tokens.size()) || (current().type() == T_EOF);
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");

		for (Token<?> token : tokens)
			sj.add(String.format("%s", token));

		return sj.toString();
	}
}
