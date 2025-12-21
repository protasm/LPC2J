package io.github.protasm.lpc2j.preproc;

import java.nio.file.Path;
import java.util.Objects;

/** Result of resolving an include directive. */
public record IncludeResolution(String source, Path resolvedPath, String displayPath) {
  public IncludeResolution {
    Objects.requireNonNull(source, "source");
  }
}
