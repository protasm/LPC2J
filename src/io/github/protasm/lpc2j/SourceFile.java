package io.github.protasm.lpc2j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SourceFile {
	private final String basePath;
	private final String relPath;
	private final Path fullPath;
	private final String slashName;
	private final String dotName;

	public SourceFile(String basePath, String relPath) {
		this.basePath = basePath;
		this.relPath = relPath;

		this.fullPath = Paths.get(basePath, relPath);
		this.slashName = toSlashNotation(relPath);
		this.dotName = toDotNotation(relPath);
	}

	public String basePath() {
		return basePath;
	}

	public String relPath() {
		return relPath;
	}

	public Path fullPath() {
		return fullPath;
	}

	public String slashName() {
		return slashName;
	}

	public String dotName() {
		return dotName;
	}

	public String source() {
		try {
			return Files.readString(fullPath);
		} catch (IOException e) {
			System.out.println("Failed to read file: " + fullPath + ".");
			
			return null;
		}
	}

	public void write(byte[] bytes) {
		if (bytes == null) return;
		
		String str = trimFileSuffix(relPath) + ".class";

		Path writePath = Paths.get(basePath, str);

		try {
			Files.write(writePath, bytes);
		} catch (IOException e) {
			System.out.println("Failed to write file: " + writePath + ".");
			
			return;
		}

		System.out.println("File written to: " + writePath + ".");
	}

	private String toSlashNotation(String str) {
		// trim file suffix, if any
		str = trimFileSuffix(str);

		// trim leading slash, if any
		return trimLeadingSlash(str);
	}

	private String toDotNotation(String str) {
		str = toSlashNotation(str);

		// replace infix slashes with dots
		return replaceSlashesWithDots(str);
	}

	private String trimFileSuffix(String str) {
		int idx = str.lastIndexOf('.');

		return (idx == -1) ? str : str.substring(0, idx);

	}

	private String trimLeadingSlash(String str) {
		if (str.startsWith("/") || str.startsWith("\\"))
			return str.substring(1, str.length());
		else
			return str;
	}

	private String replaceSlashesWithDots(String str) {
		return str.replace("/", ".").replace("\\", ".");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("basePath=%s\n", basePath));
		sb.append(String.format("relPath=%s\n", relPath));
		sb.append(String.format("fullPath=%s\n", fullPath));
		sb.append(String.format("slashName=%s\n", slashName));
		sb.append(String.format("dotName=%s\n", dotName));

		return sb.toString();
	}
}
