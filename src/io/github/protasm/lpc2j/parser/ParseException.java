package io.github.protasm.lpc2j.parser;

import io.github.protasm.lpc2j.scanner.Token;

public class ParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int line;

    public ParseException(String message, int line) {
	super(message);

	this.line = line;
    }

    public ParseException(String message, Token<?> token) {
	this(message, token.line());
    }

    public int line() {
	return line;
    }

    @Override
    public String toString() {
	return String.format("ParseException at line %d: %s", line, getMessage());
    }
}
