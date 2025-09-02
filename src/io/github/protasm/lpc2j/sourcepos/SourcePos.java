package io.github.protasm.lpc2j.sourcepos;

/** 1-based line/column position within a logical source file. */
public record SourcePos(String file, int line, int column) {
	public SourcePos {
		if (file == null)
			throw new IllegalArgumentException("file");

		if (line < 1)
			throw new IllegalArgumentException("line must be 1+");

		if (column < 1)
			throw new IllegalArgumentException("column must be 1+");
	}

	@Override
	public String toString() {
		return file + ":" + line + ":" + column;
	}
}
