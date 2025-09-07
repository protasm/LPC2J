package io.github.protasm.lpc2j.sourcepos;

/**
 * Stateless text + stateful index; safe peeking; maintains line/column via
 * LineMap.
 */
public final class CharCursor {
	private final LineMap map;
	private int i = 0;

	public CharCursor(LineMap map) {
		this.map = map;
	}

	public int index() {
		return i;
	}

	public boolean end() {
		return i >= map.sourceLength();
	}

	public char peek() {
		return map.charAt(i);
	}

	public char peekNext() {
		return map.charAt(i + 1);
	}

	public int sourceLength() {
		return map.sourceLength();
	}

	public boolean canPeekNext() {
		return (index() + 1) < sourceLength();
	}

	/** Advance one char and return it. */
	public char advance() {
		if (end())
			return '\0'; // avoid calling peek()/charAt() at EOF

		char c = map.charAt(i);

		i++;

		return c;
	}

	/** Advance while predicate holds; returns chars consumed. */
	public int advanceWhile(java.util.function.IntPredicate pred) {
		int start = i;

		while (!end() && pred.test(map.charAt(i)))
			i++;

		return i - start;
	}

	public void rewind(int to) {
		i = Math.max(0, Math.min(to, map.sourceLength()));
	}

	/** Current 1-based source position. */
	public SourcePos pos() {
		return map.posAt(i);
	}

	/** Helper/convenience methods. */
	public String fileName() {
		return pos().fileName();
	}

	public int line() {
		return pos().line();
	}

	public int column() {
		return pos().column();
	}
}
