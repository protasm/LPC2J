package io.github.protasm.lpc2j.console.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    public Path dirAt(String vPathStr) {
	if ((vPathStr == null) || vPathStr.isBlank())
	    return Path.of("/");

	try {
	    Path resolved = resolve(vPathStr, true);

	    if ((resolved == null) || !Files.isDirectory(resolved))
		throw new IllegalArgumentException();

	    if (resolved.equals(basePath))
		return Path.of("/");

	    // Return the portion of the path after basePath
	    return basePath.relativize(resolved);
	} catch (IllegalArgumentException e) {
	    return null;
	}
    }

    public Path fileAt(String vPathStr) {
	try {
	    if ((vPathStr == null) || vPathStr.isBlank())
		throw new IllegalArgumentException();

	    Path resolved = resolve(vPathStr, true);

	    if ((resolved == null) || !Files.isRegularFile(resolved))
		throw new IllegalArgumentException();

	    return basePath.relativize(resolved);
	} catch (IllegalArgumentException e) {
	    return null;
	}
    }

    public boolean read(FSSourceFile sf) {
	try {
	    if (sf == null)
		throw new IllegalArgumentException();

	    String vPathStr = sf.vPath().toString();
	    Path resolved = resolve(vPathStr, true);

	    if ((resolved == null) || !Files.isRegularFile(resolved))
		throw new IllegalArgumentException();

	    String content = Files.readString(resolved);

	    sf.setSource(content);

	    return true;
	} catch (IOException e) {
	    System.out.println("Failed to read source file: " + sf);

	    sf.setSource(null);

	    return false;
	}
    }

    public boolean write(FSSourceFile sf) {
	try {
	    if (sf == null)
		throw new IllegalArgumentException("Source file cannot be null.");

	    String classPathStr = sf.classPath().toString();
	    Path resolved = resolve(classPathStr, false);

	    if (resolved == null)
		throw new IllegalArgumentException("Could not resolve source file path: " + classPathStr);

	    byte[] bytes = sf.bytes();

	    if (bytes == null)
		throw new IllegalArgumentException("Source file bytes are null.");

	    Files.write(resolved, bytes);

	    return true;
	} catch (IOException | IllegalArgumentException e) {
	    System.out.println("Failed to write classfile: " + sf);

	    return false;
	}
    }

    public File[] filesIn(String vPathStr) {
	try {
	    Path resolved = dirAt(vPathStr);

	    if (resolved == null)
		throw new IllegalArgumentException();

	    // If resolved is "/", use basePath directly
	    File dir = resolved.equals(Path.of("/"))
		    ? basePath.toFile()
		    : basePath.resolve(resolved).toFile();

	    return dir.listFiles();
	} catch (IllegalArgumentException e) {
	    return null;
	}
    }

    public String contentsOf(String vPathStr) {
	try {
	    if ((vPathStr == null) || vPathStr.isBlank())
		throw new IllegalArgumentException();

	    Path resolved = resolve(vPathStr, true);

	    if ((resolved == null) || !Files.isRegularFile(resolved))
		throw new IllegalArgumentException();

	    return Files.readString(resolved);
	} catch (IOException | IllegalArgumentException e) {
	    return null;
	}
    }

    private Path resolve(String vPathStr, boolean checkExists) {
	try {
	    // Concatenate basePath and vPath, normalize the result
	    Path concat = Paths.get(basePath.toString(), vPathStr);
	    Path normalized = concat.normalize();

	    // Ensure the normalized path is still within basePath
	    // and exists (optional).
	    if (!normalized.startsWith(basePath)
		    || (checkExists && !Files.exists(normalized)))
		throw new IllegalArgumentException();

	    return normalized;
	} catch (IllegalArgumentException e) {
	    return null;
	}
    }
}
