package io.github.protasm.lpc2j.fs;

import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.scanner.Tokens;

public class FSSourceFile {
    private final String vPath;
    private final String fileName;

    private String source;
    private Tokens tokens;
    private ASTObject astObject;
    private byte[] bytes;
    private Object lpcObject;

    public FSSourceFile(String vPath, String fileName) {
	this.vPath = vPath;
	this.fileName = fileName;

	if (!validExtension(fileName))
	    throw new IllegalArgumentException("Invalid source file name.");
    }

    public String vPath() {
	return vPath;
    }

    public String fileName() {
	return fileName;
    }

    public String prefix() {
	int idx = fileName.lastIndexOf('.');

	return fileName.substring(0, idx);
    }

    public String extension() {
	int idx = fileName.lastIndexOf('.');

	return fileName.substring(idx + 1);
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
	return trimLeadingSlash(vPath);
    }

    public String dotName() {
	String str = slashName();

	// replace infix slashes with dots
	return str.replace("/", ".").replace("\\", ".");
    }

    private String trimLeadingSlash(String str) {
	if (str.startsWith("/") || str.startsWith("\\"))
	    return str.substring(1, str.length());
	else
	    return str;
    }

    private boolean validExtension(String fileName) {
	return (fileName.endsWith(".lpc") || fileName.endsWith(".c"));
    }
}
