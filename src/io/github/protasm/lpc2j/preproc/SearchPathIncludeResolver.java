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

    if (this.baseIncludePath != null) {
      Path includeDir = this.baseIncludePath.resolve("include").normalize();

      if (dedup.add(includeDir)) {
        roots.add(includeDir);
      }
    }

    // Always search the base include path last to preserve the previous fallback behavior.
    if ((this.baseIncludePath != null) && dedup.add(this.baseIncludePath)) {
      roots.add(this.baseIncludePath);
    }

    this.systemIncludeRoots = List.copyOf(roots);
  }

  @Override
  public IncludeResolution resolve(Path includingFile, String includePath, boolean system)
      throws IOException {
    if (includePath == null) throw new IOException("include path cannot be null");

    if (!system && (includingFile != null)) {
      Path parent = resolveAgainstBase(includingFile.getParent());

      if (parent != null) {
        Path maybe = tryRead(parent, includePath);

        if (maybe != null) return buildResolution(maybe);
      }
    }

    if (system) {
      for (Path root : systemIncludeRoots) {
        Path maybe = tryRead(root, includePath);

        if (maybe != null) return buildResolution(maybe);
      }
    } else if (baseIncludePath != null) {
      Path maybe = tryRead(baseIncludePath, includePath);

      if (maybe != null) return buildResolution(maybe);
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

  private Path tryRead(Path root, String includePath) throws IOException {
    if (root == null) return null;

    Path anchoredRoot = resolveAgainstBase(root);
    Path candidate = anchoredRoot.resolve(includePath).normalize();

    if (!candidate.startsWith(anchoredRoot.normalize())) return null;

    if (Files.isRegularFile(candidate)) return candidate;

    return null;
  }

  private Path resolveAgainstBase(Path path) {
    if (path == null) return null;
    Path normalized = path.normalize();

    if (baseIncludePath == null) return normalized;

    if (normalized.startsWith(baseIncludePath)) return normalized;

    if (normalized.isAbsolute() && Files.exists(normalized)) return normalized;

    String relative =
        (normalized.getRoot() == null)
            ? normalized.toString()
            : normalized.toString().substring(normalized.getRoot().toString().length());

    normalized = baseIncludePath.resolve(relative);

    return normalized.normalize();
  }

  private IncludeResolution buildResolution(Path candidate) throws IOException {
    return new IncludeResolution(Files.readString(candidate), candidate, displayPath(candidate));
  }

  private String displayPath(Path candidate) {
    if ((candidate == null) || (baseIncludePath == null)) return (candidate != null) ? candidate.toString() : null;

    Path normalizedCandidate = candidate.normalize();

    if (normalizedCandidate.startsWith(baseIncludePath)) {
      Path rel = baseIncludePath.relativize(normalizedCandidate);

      if (rel.getNameCount() == 0) return "/";

      return "/" + rel.toString().replace('\\', '/');
    }

    return normalizedCandidate.toString();
  }
}
