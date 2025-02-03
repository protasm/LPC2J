package io.github.protasm.lpc2j.fs;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class FSFile {
	protected final String basePath;
	protected final String relPath;

	public FSFile(String basePath, String relPath) {
		this.basePath = basePath;
		this.relPath = relPath;
	}

	public String basePath() {
		return basePath;
	}

	public String relPath() {
		return relPath;
	}

	public Path fullPath() {
		return Paths.get(basePath, relPath);
	}

	public String slashName() {
		// trim file suffix, if any
		str = trimFileSuffix(str);

		// trim leading slash, if any
		str = trimLeadingSlash(str);
		
		return str;
	}

	public String dotName() {
		str = slashName(str);

		// replace infix slashes with dots
		str = str.replace("/", ".").replace("\\", ".");
		
		return str;
	}

	protected String trimFileSuffix(String str) {
		int idx = str.lastIndexOf('.');

		return (idx == -1) ? str : str.substring(0, idx);

	}

	protected String trimLeadingSlash(String str) {
		if (str.startsWith("/") || str.startsWith("\\"))
			return str.substring(1, str.length());
		else
			return str;
	}

	protected String replaceSlashesWithDots(String str) {
		return 
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
