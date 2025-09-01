package io.github.protasm.lpc2j.preproc;

public class PreprocessException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final String file;
	private final int line;

	public PreprocessException(String message, String file, int line) {
		super(message);

		this.file = file;
		this.line = line;
	}

	@Override
	public String getMessage() {
		return String.format("%s (at %s:%d)", super.getMessage(), file, line);
	}
}
