package io.github.protasm.lpc2j.fs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FSVirtualPath {
    private final FSBasePath basePath;
    private final Path vPath;

    public FSVirtualPath(String basePathStr, String vPathStr) {
	basePath = new FSBasePath(basePathStr);

	Path parsedVPath = Paths.get(vPathStr).normalize();
	Path combined;

	if (parsedVPath.isAbsolute()) {
	    if (!parsedVPath.startsWith(base))
		throw new IllegalArgumentException("Invalid virtual path: " + vPathStr);

	    this.vPath = base.relativize(parsedVPath);

	    combined = parsedVPath;
	} else {
	    combined = base.resolve(parsedVPath).normalize();

	    this.vPath = parsedVPath;
	}

	if (!Files.exists(combined) || !Files.isDirectory(combined))
	    throw new IllegalArgumentException(
		    "The combined path (basePath + vPath) must be a valid directory: " + combined);
    }

    public Path vPath() {
	return vPath;
    }

    private class FSBasePath {
	private final Path basePath;

	public FSBasePath(String basePathStr) {
	    Path base = Paths.get(basePathStr).toAbsolutePath().normalize();

	    if (!Files.exists(base) || !Files.isDirectory(base))
		throw new IllegalArgumentException("Invalid base path: " + basePathStr);

	    this.basePath = base;
	}

	public Path basePath() {
	    return basePath;
	}
    }
}
