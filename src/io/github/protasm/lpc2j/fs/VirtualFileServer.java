package io.github.protasm.lpc2j.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.protasm.lpc2j.FSSourceFile;

/**
 * Simple filesystem-backed "virtual" file server rooted at a fixed base
 * directory. Callers interact with relative paths; this class keeps them
 * confined to the base and provides convenience methods for resolving
 * directories/files and reading/writing {@link FSSourceFile} contents.
 */
public class VirtualFileServer {
    private final Path basePath;

    public VirtualFileServer(String basePathStr) {
        Path path = Paths.get(basePathStr);

        if (!path.isAbsolute())
            throw new IllegalArgumentException("basePath must be absolute");

        this.basePath = path.normalize();
    }

    public Path basePath() {
        return basePath;
    }

    /**
     * Resolve a subdirectory relative to the base. Returns the relative path if
     * it exists (or the absolute base path when {@code relPath} is blank),
     * otherwise {@code null}.
     */
    public Path dirAt(String relPath) {
        if (relPath == null)
            return null;

        if (relPath.isBlank())
            return basePath;

        Path resolved = resolve(relPath);

        if ((resolved != null) && Files.isDirectory(resolved))
            return basePath.relativize(resolved);

        return null;
    }

    /**
     * Resolve a file relative to the base, returning the relative path if it
     * exists and is a regular file. Returns {@code null} for blank/invalid
     * paths, directories, or escapes outside the base.
     */
    public Path fileAt(String relPath) {
        if ((relPath == null) || relPath.isBlank())
            return null;

        Path resolved = resolve(relPath);

        if ((resolved != null) && Files.isRegularFile(resolved))
            return basePath.relativize(resolved);

        return null;
    }

    /** List the files within the given subdirectory (or base when blank). */
    public File[] filesIn(String relPath) {
        if (relPath == null)
            return null;

        Path target;

        if (relPath.isBlank()) {
            target = basePath;
        } else {
            Path resolved = resolve(relPath);

            if ((resolved == null) || !Files.isDirectory(resolved))
                return null;

            target = resolved;
        }

        return target.toFile().listFiles();
    }

    /** Read a source file's contents into the provided {@link FSSourceFile}. */
    public boolean read(FSSourceFile sf) {
        if (sf == null)
            return false;

        Path abs = basePath.resolve(sf.relativePath()).normalize();

        if (!abs.startsWith(basePath) || !Files.isRegularFile(abs))
            return false;

        try {
            sf.setSource(Files.readString(abs));

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Write compiled bytes from the {@link FSSourceFile} back to disk. */
    public boolean write(FSSourceFile sf) {
        if ((sf == null) || (sf.bytes() == null))
            return false;

        Path abs = basePath.resolve(sf.relativePath()).normalize();

        if (!abs.startsWith(basePath))
            return false;

        try {
            Files.createDirectories(abs.getParent());
            Files.write(abs, sf.bytes());

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Path resolve(String relPath) {
        Path rel = Path.of(relPath).normalize();

        if (rel.isAbsolute())
            return null;

        Path resolved = basePath.resolve(rel).normalize();

        if (!resolved.startsWith(basePath))
            return null;

        return resolved;
    }
}
