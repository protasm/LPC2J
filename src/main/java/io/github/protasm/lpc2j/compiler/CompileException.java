package io.github.protasm.lpc2j.compiler;

public class CompileException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CompileException(String message) {
        super(message);
    }

    public CompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
