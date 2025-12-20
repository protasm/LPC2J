package io.github.protasm.lpc2j.sourcepos;

import java.util.Objects;

/**
 * Inclusive start, exclusive end span within a single logical source file.
 *
 * <p>Both endpoints retain filename, line/column, and absolute offset information to keep
 * diagnostics precise even after preprocessing.</p>
 */
public record SourceSpan(SourcePos start, SourcePos end) {
    public SourceSpan {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");

        if (!start.fileName().equals(end.fileName()))
            throw new IllegalArgumentException("Span endpoints must share the same file.");
    }

    public String fileName() {
        return start.fileName();
    }

    public int startLine() {
        return start.line();
    }

    public int startColumn() {
        return start.column();
    }

    public int startOffset() {
        return start.offset();
    }

    public int endLine() {
        return end.line();
    }

    public int endColumn() {
        return end.column();
    }

    public int endOffset() {
        return end.offset();
    }

    public static SourceSpan from(LineMap map, int startOffset, int endOffset) {
        Objects.requireNonNull(map, "map");

        int safeStart = Math.max(0, startOffset);
        int safeEnd = Math.max(safeStart, Math.min(endOffset, map.sourceLength()));

        SourcePos startPos = map.posAt(safeStart);
        SourcePos endPos = map.posAt(safeEnd);

        return new SourceSpan(startPos, endPos);
    }

    public static SourceSpan encompassing(SourceSpan a, SourceSpan b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");

        if (!a.fileName().equals(b.fileName()))
            throw new IllegalArgumentException("Cannot encompass spans from different files.");

        SourcePos start = (a.startOffset() <= b.startOffset()) ? a.start() : b.start();
        SourcePos end = (a.endOffset() >= b.endOffset()) ? a.end() : b.end();

        return new SourceSpan(start, end);
    }
}
