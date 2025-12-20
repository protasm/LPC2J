package io.github.protasm.lpc2j.preproc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Include resolver that searches a configurable list of system include roots. */
public final class SearchPathIncludeResolver implements IncludeResolver {
  private final Path baseIncludePath;
  private final List<Path> systemIncludeRoots;

  public SearchPathIncludeResolver(Path baseIncludePath, List<Path> systemIncludePaths) {
    this.baseIncludePath = (baseIncludePath == null) ? null : baseIncludePath.normalize();

    List<Path> roots = new ArrayList<>();
    Set<Path> dedup = new HashSet<>();

    if (systemIncludePaths != null) {
      for (Path p : systemIncludePaths) {
        if (p == null) continue;

        Path normalized = normalizeAgainstBase(p);

        if (dedup.add(normalized)) roots.add(normalized);
      }
    }

    // Always search the base include path last to preserve the previous fallback behavior.
    if ((this.baseIncludePath != null) && dedup.add(this.baseIncludePath)) {
      roots.add(this.baseIncludePath);
    }

    this.systemIncludeRoots = List.copyOf(roots);
  }

  @Override
  public String resolve(Path includingFile, String includePath, boolean system) throws IOException {
    if (includePath == null) throw new IOException("include path cannot be null");

    if (!system && (includingFile != null)) {
      Path parent = includingFile.getParent();

      if (parent != null) {
        String maybe = tryRead(parent, includePath);

        if (maybe != null) return maybe;
      }
    }

    if (system) {
      for (Path root : systemIncludeRoots) {
        String maybe = tryRead(root, includePath);

        if (maybe != null) return maybe;
      }
    } else if (baseIncludePath != null) {
      String maybe = tryRead(baseIncludePath, includePath);

      if (maybe != null) return maybe;
    }

    throw new IOException("cannot include '" + includePath + "'");
  }

  private Path normalizeAgainstBase(Path path) {
    Objects.requireNonNull(path);

    Path normalized = path;

    if (!path.isAbsolute() && (baseIncludePath != null)) {
      normalized = baseIncludePath.resolve(path);
    }

    return normalized.normalize();
  }

  private String tryRead(Path root, String includePath) throws IOException {
    Path candidate = root.resolve(includePath).normalize();

    if (!candidate.startsWith(root.normalize())) return null;

    if (Files.isRegularFile(candidate)) return Files.readString(candidate);

    return null;
  }
}
