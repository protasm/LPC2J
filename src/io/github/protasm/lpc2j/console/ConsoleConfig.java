package io.github.protasm.lpc2j.console;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight configuration loader for console settings.
 *
 * <p>Currently understands the MudOS-style {@code include directories} entry in {@code mudos.cfg}.
 */
public final class ConsoleConfig {
  private static final String CONFIG_FILE = "mudos.cfg";

  private final List<Path> includeDirs;

  private ConsoleConfig(List<Path> includeDirs) {
    this.includeDirs = List.copyOf(includeDirs);
  }

  public static ConsoleConfig load(Path basePath) {
    List<Path> includeDirs = new ArrayList<>();

    if (basePath != null) includeDirs.add(basePath.normalize());

    Path cfgPath = (basePath == null) ? Path.of(CONFIG_FILE) : basePath.resolve(CONFIG_FILE);

    if (Files.isRegularFile(cfgPath)) {
      try {
        parseConfig(basePath, includeDirs, cfgPath);
      } catch (IOException e) {
        // Fall back to defaults if the config cannot be read.
      }
    }

    return new ConsoleConfig(includeDirs);
  }

  public List<Path> includeDirs() {
    return includeDirs;
  }

  private static void parseConfig(Path basePath, List<Path> includeDirs, Path cfgPath)
      throws IOException {
    for (String line : Files.readAllLines(cfgPath)) {
      String trimmed = line.trim();

      if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

      int colonIdx = trimmed.indexOf(':');

      if (colonIdx <= 0) continue;

      String key = trimmed.substring(0, colonIdx).trim().toLowerCase();
      String value = trimmed.substring(colonIdx + 1).trim();

      if ("include directories".equals(key)) {
        for (String entry : value.split(":")) {
          String dir = entry.trim();

          if (dir.isEmpty()) continue;

          Path p = Path.of(dir);

          if ((basePath != null) && !p.isAbsolute()) p = basePath.resolve(p);

          p = p.normalize();

          if (!includeDirs.contains(p)) includeDirs.add(p);
        }
      }
    }
  }
}
