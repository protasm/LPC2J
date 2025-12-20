package io.github.protasm.lpc2j.parser;

import io.github.protasm.lpc2j.sourcepos.SourceSpan;
import io.github.protasm.lpc2j.token.Token;

public class ParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int line;
    private final SourceSpan span;

    public ParseException(String message, int line) {
        this(message, line, null);
    }

    public ParseException(String message, int line, Throwable cause) {
        super(message, cause);

        this.line = line;
        this.span = null;
    }

    public ParseException(String message, SourceSpan span) {
        this(message, span, null);
    }

    public ParseException(String message, SourceSpan span, Throwable cause) {
        super(message, cause);

        this.span = span;
        this.line = (span != null) ? span.startLine() : -1;
    }

    public ParseException(String message, Token<?> token) {
        this(message, token != null ? token.span() : null);
    }

    public ParseException(String message, Token<?> token, Throwable cause) {
        this(message, token != null ? token.span() : null, cause);
    }

    public ParseException(String message) {
        this(message, -1); // TODO

    }

    public int line() {
        return line;
    }

    public SourceSpan span() {
        return span;
    }

    @Override
    public String toString() {
        if (span != null)
            return String.format("ParseException at %s:%d:%d: %s", span.fileName(), span.startLine(),
                    span.startColumn(), getMessage());

        if (line < 0)
            return String.format("ParseException: %s", getMessage());

        return String.format("ParseException at line %d: %s", line, getMessage());
    }
}
