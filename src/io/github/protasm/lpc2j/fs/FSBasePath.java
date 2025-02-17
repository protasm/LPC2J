package io.github.protasm.lpc2j.fs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FSBasePath {
    private final Path basePath;

    public FSBasePath(String basePathStr) {
	Path path = Path.of(basePathStr).normalize();

	if (!path.isAbsolute())
	    throw new IllegalArgumentException("Base path must be absolute: " + basePathStr);

	this.basePath = path;
    }

    public Path basePath() {
	return basePath;
    }

    public Path resolve(Path vPath) {
	if (vPath == null)
	    throw new InvalidPathException("null", "Invalid path");

	// Concatenate basePath and vPath, normalize the result
	Path concat = Paths.get(basePath.toString(), vPath.toString());
	Path normalized = concat.normalize();

	if (normalized.compareTo(basePath) == 0) // same
	    return Path.of("/");

	// Ensure the resolved path is still within basePath and is a directory
	if (!normalized.startsWith(basePath) || !Files.isDirectory(normalized))
	    throw new InvalidPathException(vPath.toString(), "Invalid path");

	// Return the portion of the path after basePath
	return basePath.relativize(normalized);
    }

    public File[] filesIn(Path vPath) {
	Path resolved = resolve(vPath);

	// If resolved is "/", use basePath directly
	File dir = resolved.equals(Path.of("/"))
		? basePath.toFile()
		: basePath.resolve(resolved).toFile();

	return dir.listFiles();
    }
}
