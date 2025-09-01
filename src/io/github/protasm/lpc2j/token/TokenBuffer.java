package io.github.protasm.lpc2j.token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable storage for a scanned token sequence, plus a stateful Cursor that
 * provides parser-friendly operations (peek/consume/mark).
 */
public final class TokenBuffer {
	private final List<Token<?>> tokens; // sealed
	private final Token<?> eof;

	private TokenBuffer(List<Token<?>> sealed, Token<?> eof) {
		this.tokens = sealed;
		this.eof = eof;
	}

	/** Build from a mutable list (Scanner fills it), then seal. */
	public static TokenBuffer of(List<Token<?>> scanned, Token<?> eof) {
		Objects.requireNonNull(scanned, "scanned");
		Objects.requireNonNull(eof, "eof");

		return new TokenBuffer(Collections.unmodifiableList(new ArrayList<>(scanned)), eof);
	}

	public int size() {
		return tokens.size();
	}

	/** Stateless peek into the sealed buffer. */
	public Token<?> peekAt(int index) {
		return ((index >= 0) && (index < tokens.size())) ? tokens.get(index) : eof;
	}

	/** Start a new independent cursor at position 0. */
	public Cursor cursor() {
		return new Cursor(0);
	}

	/** Parser-facing reader over this buffer. */
	public final class Cursor {
		private int index;

		private Cursor(int start) {
			this.index = start;
		}

		public int position() {
			return index;
		}

		public boolean isAtEnd() {
			return index >= tokens.size();
		}

		public int mark() {
			return index;
		}

		public void rewind(int mark) {
			if ((mark < 0) || (mark > tokens.size()))
				throw new IllegalArgumentException("Bad mark: " + mark);

			index = mark;
		}

		/** Lookahead where peek(0)==current(). Never throws; returns EOF past end. */
		public Token<?> peek(int k) {
			int i = index + k;

			return ((i >= 0) && (i < tokens.size())) ? tokens.get(i) : eof;
		}

		public Token<?> current() {
			return peek(0);
		}

		public Token<?> previous() {
			return ((index - 1) >= 0) ? tokens.get(index - 1) : eof;
		}

		/** Advance by one; stays on EOF once past end. Returns the consumed token. */
		public Token<?> advance() {
			Token<?> t = current();

			if (!isAtEnd())
				index++;

			return t;
		}

		/** True if current token matches any of the given types (no consume). */
		public boolean check(TokenType... types) {
			TokenType tt = current().type();

			for (TokenType want : types)
				if (tt == want)
					return true;

			return false;
		}

		/** If current matches any type, consume and return true; else false. */
		public boolean match(TokenType... types) {
			if (check(types)) {
				advance();

				return true;
			}

			return false;
		}

		/**
		 * Consume exactly one of the expected types or throw. Returns the consumed
		 * token.
		 */
		public Token<?> consume(String messageIfMismatch, TokenType... expected) {
			if (match(expected))
				return previous();

			Token<?> got = current();

			throw new ParseError(expectMsg(messageIfMismatch, expected, got));
		}

		/**
		 * Consume repeated occurrences of a single type. Returns the last consumed or
		 * EOF if none.
		 */
		public Token<?> advanceThrough(TokenType type) {
			Token<?> last = eof;

			while (check(type))
				last = advance();

			return last;
		}

		private String expectMsg(String msg, TokenType[] exp, Token<?> got) {
			StringBuilder sb = new StringBuilder();

			if ((msg != null) && !msg.isEmpty())
				sb.append(msg).append(" ");

			sb.append("expected ");

			for (int i = 0; i < exp.length; i++) {
				if (i > 0)
					sb.append(i == (exp.length - 1) ? " or " : ", ");

				sb.append(exp[i]);
			}

			sb.append(", but found ").append(got.type());

			// If Token exposes lexeme/line, append for clarity:
			// sb.append(" at line ").append(got.line()).append(" near
			// '").append(got.lexeme()).append("'");

			return sb.toString();
		}
	}

	/**
	 * Lightweight unchecked error a parser can catch at statement/decl boundaries.
	 */
	public static final class ParseError extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ParseError(String message) {
			super(message);
		}
	}
}
