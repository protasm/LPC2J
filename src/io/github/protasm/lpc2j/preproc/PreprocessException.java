package io.github.protasm.lpc2j.preproc;

public class PreprocessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String fileName;
    private final int line;

    public PreprocessException(String message, String fileName, int line) {
        super(message);

        this.fileName = fileName;
        this.line = line;
    }

    public PreprocessException(String message, String fileName, int line, Throwable cause) {
        super(message, cause);

        this.fileName = fileName;
        this.line = line;
    }

    @Override
    public String getMessage() {
        return String.format("%s (at %s:%d)", super.getMessage(), fileName, line);
    }
}
