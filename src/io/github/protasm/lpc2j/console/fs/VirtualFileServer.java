package io.github.protasm.lpc2j.console.fs;

import java.io.File;
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
