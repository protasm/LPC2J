package io.github.protasm.lpc2j.scanner;

public class ScanException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final int line;

	public ScanException(String message, int line) {
		super(message);

		this.line = line;
	}

	public ScanException(String message, Token<?> token) {
		this(message, token.line());
	}

	public int line() {
		return line;
	}

	@Override
	public String toString() {
		return String.format("ScanException at line %d: %s", line, getMessage());
	}
}
