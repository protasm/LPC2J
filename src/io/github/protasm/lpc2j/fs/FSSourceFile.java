package io.github.protasm.lpc2j.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FSSourceFile {
    protected final Path basePath;
    protected final Path relPath;

    public FSSourceFile(String basePath, String relPath) {
	if (!relPath.endsWith(".lpc"))
	    throw new IllegalArgumentException("Source file name must end with '.lpc' extension.");

	this.basePath = Paths.get(basePath);
	this.relPath = Paths.get(relPath);
    }

    public Path basePath() {
	return basePath;
    }

    public Path relPath() {
	return relPath;
    }

    public Path fullPath() {
	return basePath.resolve(relPath);
    }

    public String slashName() {
	// trim file suffix, if any
	String str = trimFileSuffix(relPath);

	// trim leading slash, if any
	str = trimLeadingSlash(str);

	return str;
    }

    public String dotName() {
	String str = slashName();

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

    public String source() {
	try {
	    return Files.readString(fullPath());
	} catch (IOException e) {
	    System.out.println("Failed to read file: " + relPath + ".");

	    return null;
	}
    }

    public void write(byte[] bytes) {
	if (bytes == null)
	    return;

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

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("basePath=%s\n", basePath));
	sb.append(String.format("relPath=%s\n", relPath));
	sb.append(String.format("fullPath=%s\n", fullPath()));
	sb.append(String.format("slashName=%s\n", slashName()));
	sb.append(String.format("dotName=%s\n", dotName()));

	return sb.toString();
    }
}
