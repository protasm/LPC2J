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

    public Path dirAt(String vPath) {
	if ((vPath == null) || vPath.isBlank())
	    return Path.of("/");

	try {
	    // Concatenate basePath and vPath, normalize the result
	    Path concat = Paths.get(basePath.toString(), vPath);
	    Path normalized = concat.normalize();

	    if (normalized.compareTo(basePath) == 0) // same
		return Path.of("/");

	    // Ensure the resolved path is still within basePath
	    // and is an existing directory
	    if (!normalized.startsWith(basePath)
		    || !Files.exists(normalized)
		    || !Files.isDirectory(normalized))
		return null;

	    // Return the portion of the path after basePath
	    return basePath.relativize(normalized);
	} catch (InvalidPathException e) {
	    return null;
	}
    }

    public File fileAt(String vPath) {
	if ((vPath == null) || vPath.isBlank())
	    return null;

	try {
	    // Concatenate basePath and vPath, normalize the result
	    Path concat = Paths.get(basePath.toString(), vPath);
	    Path normalized = concat.normalize();

	    // Ensure the resolved path is still within basePath
	    // and is an existing directory
	    if (!normalized.startsWith(basePath)
		    || !Files.exists(normalized)
		    || Files.isDirectory(normalized))
		return null;
	    
	    return new File(normalized.toString());
	} catch (InvalidPathException e) {
	    return null;
	}
    }

    public File[] filesIn(String vPath) {
	Path resolved = dirAt(vPath);

	if (resolved == null)
	    return null;

	// If resolved is "/", use basePath directly
	File dir = resolved.equals(Path.of("/"))
		? basePath.toFile()
		: basePath.resolve(resolved).toFile();

	return dir.listFiles();
    }
}
