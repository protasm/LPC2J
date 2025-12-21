package io.github.protasm.lpc2j.console;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight configuration loader for console settings.
 *
 * <p>Parses a provided configuration file for the mudlib base path and one or more include search
 * directories. Recognized keys:
 *
 * <ul>
 *   <li>{@code mudlib directory} — required; may be absolute or relative to the config file</li>
 *   <li>{@code system include directories} or {@code include directories} — optional; colon-separated
 *       list resolved relative to the mudlib directory unless absolute; order preserved</li>
 * </ul>
 */
public final class ConsoleConfig {
  private final Path basePath;
  private final List<Path> includeDirs;

  private ConsoleConfig(Path basePath, List<Path> includeDirs) {
    this.basePath = basePath;
    this.includeDirs = List.copyOf(includeDirs);
  }

  public static ConsoleConfig load(Path configPath) {
    if (configPath == null) throw new IllegalArgumentException("Configuration path cannot be null");
    if (!Files.isRegularFile(configPath))
      throw new IllegalArgumentException("Configuration file does not exist: " + configPath);

    Path configDir = configPath.toAbsolutePath().getParent();
    String mudlibDirRaw = null;
    List<String> includeEntries = new ArrayList<>();

    try {
      for (String line : Files.readAllLines(configPath)) {
        String trimmed = line.trim();

        if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

        int colonIdx = trimmed.indexOf(':');

        if (colonIdx <= 0) continue;

        String key = trimmed.substring(0, colonIdx).trim().toLowerCase();
        String value = trimmed.substring(colonIdx + 1).trim();

        switch (key) {
          case "mudlib directory" -> mudlibDirRaw = value;
          case "system include directories", "include directories" -> {
            for (String entry : value.split(":")) {
              String dir = entry.trim();
              if (!dir.isEmpty()) includeEntries.add(dir);
            }
          }
          default -> {}
        }
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to read configuration: " + e.getMessage(), e);
    }

    if ((mudlibDirRaw == null) || mudlibDirRaw.isBlank()) {
      throw new IllegalArgumentException("Configuration missing required 'mudlib directory' entry");
    }

    Path basePath = Path.of(mudlibDirRaw);
    if ((configDir != null) && !basePath.isAbsolute()) basePath = configDir.resolve(basePath);
    basePath = basePath.normalize();

    if (!Files.isDirectory(basePath)) {
      throw new IllegalArgumentException("Mudlib directory does not exist: " + basePath);
    }

    List<Path> includeDirs = new ArrayList<>();
    for (String entry : includeEntries) {
      Path p = Path.of(entry);
      if (!p.isAbsolute()) p = basePath.resolve(p);
      p = p.normalize();
      if (!includeDirs.contains(p)) includeDirs.add(p);
    }

    return new ConsoleConfig(basePath, includeDirs);
  }

  public Path basePath() {
    return basePath;
  }

  public List<Path> includeDirs() {
    return includeDirs;
  }
}
