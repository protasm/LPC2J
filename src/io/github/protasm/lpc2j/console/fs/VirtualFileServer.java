package io.github.protasm.lpc2j.console.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VirtualFileServer {
    private final Path path;

    public VirtualFileServer(String basePath) {
        Path path = Path.of(basePath).normalize();

        if (!path.isAbsolute())
            throw new IllegalArgumentException("Base path not absolute: " + basePath);

        this.path = path;
    }

    public Path basePath() {
        return path;
    }

    public Path dirAt(String vPath) {
        if (vPath == null) return null;
        if (vPath.isBlank()) return path;

        try {
            Path resolved = resolve(vPath, true);

            if ((resolved == null) || !Files.isDirectory(resolved))
                return null;

            if (resolved.equals(path))
                return Path.of("/");

            // Return the portion of the path after base path
            return path.relativize(resolved);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Path fileAt(String vPath) {
        try {
            if ((vPath == null) || vPath.isBlank())
                throw new IllegalArgumentException();

            Path resolved = resolve(vPath, true);

            if ((resolved == null) || !Files.isRegularFile(resolved))
                throw new IllegalArgumentException();

            return path.relativize(resolved);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

        public File[] filesIn(String vPathStr) {
                try {
                        Path resolved = dirAt(vPathStr);

            if (resolved == null)
                throw new IllegalArgumentException();

            // If resolved is "/", use path directly
            File dir = resolved.equals(Path.of("/")) ? path.toFile() : path.resolve(resolved).toFile();

                        return dir.listFiles();
                } catch (IllegalArgumentException e) {
                        return null;
                }
        }

        public boolean read(FSSourceFile sf) {
                try {
                        Path resolved = resolve(sf.vPath().toString(), true);

                        if ((resolved == null) || !Files.isRegularFile(resolved))
                                return false;

                        sf.setSource(Files.readString(resolved));

                        return true;
                } catch (IOException e) {
                        return false;
                }
        }

        public boolean write(FSSourceFile sf) {
                try {
                        if (sf.bytes() == null)
                                return false;

                        Path target = path.resolve(sf.classPath()).normalize();

                        if (!target.startsWith(path))
                                return false;

                        if (target.getParent() != null)
                                Files.createDirectories(target.getParent());

                        Files.write(target, sf.bytes());

                        return true;
                } catch (IOException | IllegalArgumentException e) {
                        return false;
                }
        }

        public String contentsOfFileAt(String vPathStr) {
                try {
                        Path resolved = resolve(vPathStr, true);

                        if ((resolved == null) || !Files.isRegularFile(resolved))
                                return null;

                        return Files.readString(resolved);
                } catch (IOException | IllegalArgumentException e) {
                        return null;
                }
        }

        private Path resolve(String vPath, boolean checkExists) {
        try {
            // Concatenate base path and vPath, normalize the result
            Path concat = Paths.get(path.toString(), vPath);
            Path normalized = concat.normalize();

            // Ensure the normalized path is still within path
            // and exists (optional).
            if (!normalized.startsWith(path) || (checkExists && !Files.exists(normalized)))
                throw new IllegalArgumentException();

            return normalized;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
