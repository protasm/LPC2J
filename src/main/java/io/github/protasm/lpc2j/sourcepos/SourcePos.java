package io.github.protasm.lpc2j.sourcepos;

/** 1-based line/column position within a logical source file with absolute offset. */
public record SourcePos(String fileName, int line, int column, int offset) {
    public SourcePos {
        if (fileName == null)
            throw new IllegalArgumentException("null file name");

        if (line < 1)
            throw new IllegalArgumentException("line must be 1+");

        if (column < 1)
            throw new IllegalArgumentException("column must be 1+");

        if (offset < 0)
            throw new IllegalArgumentException("offset must be 0+");
    }

    @Override
    public String toString() {
        return fileName + ":" + line + ":" + column;
    }
}
