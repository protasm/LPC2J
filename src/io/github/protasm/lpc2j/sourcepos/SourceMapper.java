package io.github.protasm.lpc2j.sourcepos;

/**
 * Maps offsets in generated/processed text back to the original source.
 */
public interface SourceMapper {
    /** Map an offset in the generated text back to its original {@link SourcePos}. */
    SourcePos originalPos(int generatedOffset);

    /**
     * Map a half-open range <code>[generatedStart, generatedEnd)</code> in the generated text back
     * to an original {@link SourceSpan}.
     */
    SourceSpan originalSpan(int generatedStart, int generatedEnd);
}
