package io.github.protasm.lpc2j.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.scanner.Tokens;

public class FSSourceFile {
    private final Path filePath;

    private final String source;
    private Tokens tokens;
    private ASTObject astObject;
    private byte[] bytes;
    private Object lpcObject;

    public FSSourceFile(Path filePath) throws IllegalArgumentException, IOException {
	if (!validExtension(filePath))
	    throw new IllegalArgumentException("Source file name must end with '.lpc' extension.");

	this.filePath = filePath;

	source = Files.readString(filePath);

	tokens = null;
	astObject = null;
	bytes = null;
	lpcObject = null;
    }

    private boolean validExtension(Path filePath) {
	return (filePath.toString().endsWith(".lpc") || filePath.toString().endsWith(".c"));
    }

    public Path filePath() {
	return filePath;
    }

    public String source() {
	return source;
    }

    public Tokens tokens() {
	return tokens;
    }

    public void setTokens(Tokens tokens) {
	this.tokens = tokens;
    }

    public ASTObject astObject() {
	return astObject;
    }

    public void setASTObject(ASTObject astObject) {
	this.astObject = astObject;
    }

    public byte[] bytes() {
	return bytes;
    }

    public void setBytes(byte[] bytes) {
	this.bytes = bytes;
    }

    public Object lpcObject() {
	return lpcObject;
    }

    public void setLPCObject(Object lpcObject) {
	this.lpcObject = lpcObject;
    }

    public void write() {
	if (bytes == null) {
	    System.out.println("Write failed: no source file bytes to write.");

	    return;
	}

	String str = stripExtension(filePath) + ".class";
	Path fullPath = Path.of(basePath, str);

	try {
	    Files.write(fullPath, bytes);
	} catch (IOException e) {
	    System.out.println("Failed to write file: " + str + ".");

	    return;
	}

	System.out.println("File written to: " + str + ".");
    }

    public String slashName() {
	// trim file extension, if any
	String str = stripExtension(relPath);

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

    /**
     * Converts a file path ending with ".lpc" or ".c" to a path ending with
     * ".class".
     *
     * @param filePath the original file path
     * @return a new Path with the ".class" extension
     * @throws IllegalArgumentException if the file does not end with ".lpc" or ".c"
     */
    private Path classPath() {
	String fileName = filePath.getFileName().toString();
	String className;

	if (fileName.endsWith(".lpc"))
	    className = fileName.substring(0, fileName.length() - 4) + ".class";
	else if (fileName.endsWith(".c"))
	    className = fileName.substring(0, fileName.length() - 2) + ".class";
	else
	    throw new IllegalArgumentException("File must end with '.lpc' or '.c': " + fileName);

	return filePath.getParent().resolve(className);
    }

    private String trimLeadingSlash(String str) {
	if (str.startsWith("/") || str.startsWith("\\"))
	    return str.substring(1, str.length());
	else
	    return str;
    }
}
