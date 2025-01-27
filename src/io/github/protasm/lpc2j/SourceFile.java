package io.github.protasm.lpc2j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SourceFile {
    private String sourcePath;
    private Path path;
    private String source;
    private String fileName;
    private String className;

    public SourceFile(String sourcePath) throws IOException {
	this.sourcePath = sourcePath;
	this.path = Paths.get(sourcePath);
	this.source = Files.readString(path);
	this.fileName = path.getFileName().toString();
	int dotIndex = fileName.lastIndexOf('.');
	this.className = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    public Path path() {
	return this.path;
    }

    public String source() {
	return this.source;
    }

    public String fileName() {
	return this.fileName;
    }

    public String className() {
	return this.className;
    }

    public String outputPath() {
	return sourcePath.replaceFirst("\\.lpc$", ".class");
    }
}
