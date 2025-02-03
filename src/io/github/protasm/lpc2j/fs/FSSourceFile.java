package io.github.protasm.lpc2j.fs;

import java.io.IOException;
import java.nio.file.Files;

public class FSSourceFile extends FSFile {
    public FSSourceFile(String basePath, String relPath) {
	super(basePath, relPath);
    }

    public String source() {
	try {
	    return Files.readString(fullPath);
	} catch (IOException e) {
	    System.out.println("Failed to read file: " + fullPath + ".");

	    return null;
	}
    }
}
