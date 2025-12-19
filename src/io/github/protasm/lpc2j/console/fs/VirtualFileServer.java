package io.github.protasm.lpc2j.console.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VirtualFileServer {
  private final Path basePath;

  public VirtualFileServer(String basePathStr) {
    Path basePath = Path.of(basePathStr).normalize();

    if (!basePath.isAbsolute())
      throw new IllegalArgumentException("Base path not absolute: " + basePathStr);

    this.basePath = basePath;
  }

  public Path basePath() {
    return basePath;
  }

  public Path dirAt(String vPathStr) {
    if (vPathStr == null) return null;
    if (vPathStr.isBlank()) return basePath;

    try {
      Path resolved = resolve(vPathStr, true);

      if ((resolved == null) || !Files.isDirectory(resolved)) return null;

      if (resolved.equals(basePath)) return Path.of("/");

      // Return the portion of the path after base path
      return basePath.relativize(resolved);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public Path fileAt(String vPathStr) {
    try {
      if ((vPathStr == null) || vPathStr.isBlank()) throw new IllegalArgumentException();

      Path resolved = resolve(vPathStr, true);

      if ((resolved == null) || !Files.isRegularFile(resolved))
        throw new IllegalArgumentException();

      return basePath.relativize(resolved);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public File[] filesIn(String vPathStr) {
    try {
      Path resolved = dirAt(vPathStr);

      if (resolved == null) throw new IllegalArgumentException();

      // If resolved is "/", use path directly
      File dir = resolved.equals(Path.of("/")) ? basePath.toFile() : basePath.resolve(resolved).toFile();

      return dir.listFiles();
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public boolean read(FSSourceFile sf) {
    try {
      Path resolved = resolve(sf.vPath().toString(), true);

      if ((resolved == null) || !Files.isRegularFile(resolved)) return false;

      sf.setSource(Files.readString(resolved));

      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public boolean write(FSSourceFile sf) {
    try {
      if (sf.bytes() == null) return false;

      Path target = basePath.resolve(sf.classPath()).normalize();

      if (!target.startsWith(basePath)) return false;

      if (target.getParent() != null) Files.createDirectories(target.getParent());

      Files.write(target, sf.bytes());

      return true;
    } catch (IOException | IllegalArgumentException e) {
      return false;
    }
  }

  public String contentsOfFileAt(String vPathStr) {
    try {
      Path resolved = resolve(vPathStr, true);

      if ((resolved == null) || !Files.isRegularFile(resolved)) return null;

      return Files.readString(resolved);
    } catch (IOException | IllegalArgumentException e) {
      return null;
    }
  }

  private Path resolve(String vPathStr, boolean checkExists) {
    try {
      // Concatenate base path and vPathStr, normalize the result
      Path concat = Paths.get(basePath.toString(), vPathStr);
      Path normalized = concat.normalize();

      // Ensure the normalized path is still within path
      // and exists (optional).
      if (!normalized.startsWith(basePath) || (checkExists && !Files.exists(normalized)))
        throw new IllegalArgumentException();

      return normalized;
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
