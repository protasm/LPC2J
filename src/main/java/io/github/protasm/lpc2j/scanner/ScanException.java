package io.github.protasm.lpc2j.scanner;

import io.github.protasm.lpc2j.sourcepos.SourceSpan;
import io.github.protasm.lpc2j.token.Token;

public class ScanException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int line;
    private final SourceSpan span;

    public ScanException(String message, int line) {
        this(message, line, null);
    }

    public ScanException(String message, int line, Throwable cause) {
        super(message, cause);

        this.line = line;
        this.span = null;
    }

    public ScanException(String message, SourceSpan span) {
        this(message, span, null);
    }

    public ScanException(String message, SourceSpan span, Throwable cause) {
        super(message, cause);

        this.span = span;
        this.line = (span != null) ? span.startLine() : -1;
    }

    public ScanException(String message, Token<?> token) {
        this(message, token != null ? token.span() : null);
    }

    public ScanException(String message, Token<?> token, Throwable cause) {
        this(message, token != null ? token.span() : null, cause);
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
            return String.format("ScanException at %s:%d:%d: %s", span.fileName(), span.startLine(),
                    span.startColumn(), getMessage());

        return String.format("ScanException at line %d: %s", line, getMessage());
    }
}
