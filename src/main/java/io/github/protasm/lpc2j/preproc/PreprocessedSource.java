package io.github.protasm.lpc2j.preproc;

import io.github.protasm.lpc2j.sourcepos.SourceMapper;
import java.util.Objects;

/** Result of preprocessing: expanded source text with a mapping back to originals. */
public record PreprocessedSource(String source, SourceMapper mapper) {
    public PreprocessedSource {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(mapper, "mapper");
    }
}
