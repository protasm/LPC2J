package io.github.protasm.lpc2j.fs;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class FSFile {
    protected final String basePath;
    protected final String relPath;
    protected final Path fullPath;
    protected final String slashName;
    protected final String dotName;

    public FSFile(String basePath, String relPath) {
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

    protected String toSlashNotation(String str) {
	// trim file suffix, if any
	str = trimFileSuffix(str);

	// trim leading slash, if any
	return trimLeadingSlash(str);
    }

    protected String toDotNotation(String str) {
	str = toSlashNotation(str);

	// replace infix slashes with dots
	return replaceSlashesWithDots(str);
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
