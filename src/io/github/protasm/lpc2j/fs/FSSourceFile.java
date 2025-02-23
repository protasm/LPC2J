package io.github.protasm.lpc2j.fs;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.scanner.Tokens;

public class FSSourceFile {
    private final Path vPath;

    private String source;
    private Tokens tokens;
    private ASTObject astObject;
    private byte[] bytes;
    private Object lpcObject;

    public FSSourceFile(Path vPath) {
	if (!validExtension(vPath))
	    throw new IllegalArgumentException("Invalid source file name.");

	this.vPath = vPath;
    }

    public Path vPath() {
	return vPath;
    }

    public String prefix() {
	String name = vPath.getFileName().toString();

	int idx = name.lastIndexOf('.');

	return name.substring(0, idx);
    }

    public String extension() {
	String name = vPath.getFileName().toString();

	int idx = name.lastIndexOf('.');

	return name.substring(idx + 1);
    }

    public Path classPath() {
	return Paths
		.get(
			vPath.getParent().toString(),
			prefix() + ".class");
    }

    public String source() {
	return source;
    }

    public void setSource(String source) {
	this.source = source;
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

    public String slashName() {
	return Paths
		.get(
			vPath.getParent().toString(),
			prefix())
		.toString();
    }

    public String dotName() {
	// replace infix slashes with dots
	return slashName()
		.replace("/", ".")
		.replace("\\", ".");
    }

    private boolean validExtension(Path vPath) {
	String name = vPath.getFileName().toString();

	return (name.endsWith(".lpc") || name.endsWith(".c"));
    }
}
