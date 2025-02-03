package io.github.protasm.lpc2j.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FSClassFile extends FSFile {
    public FSClassFile(String basePath, String relPath) {
	super(basePath, relPath);
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
}
