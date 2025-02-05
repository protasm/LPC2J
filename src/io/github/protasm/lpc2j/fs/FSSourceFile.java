package io.github.protasm.lpc2j.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.runtime.LPCObject;
import io.github.protasm.lpc2j.scanner.Tokens;

public class FSSourceFile {
	private final String relPath;

	private final String source;
	private Tokens tokens;
	private ASTObject astObject;
	private byte[] bytes;
	private LPCObject lpcObject;

	public FSSourceFile(String basePath, String relPath) throws IllegalArgumentException, IOException {
		if (!relPath.endsWith(".lpc"))
			throw new IllegalArgumentException("Source file name must end with '.lpc' extension.");

		this.relPath = relPath;

		source = Files.readString(Path.of(basePath, relPath));

		tokens = null;
		astObject = null;
		bytes = null;
		lpcObject = null;
	}

	public String relPath() {
		return relPath;
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

	public LPCObject lpcObject() {
		return lpcObject;
	}

	public void setLPCObject(LPCObject lpcObject) {
		this.lpcObject = lpcObject;
	}

	public void write(String basePath) {
		if (bytes == null) {
			System.out.println("Write failed: no source file bytes to write.");
			
			return;
		}

		Path fullPath = Path.of(basePath, relPath);
		String str = stripExtension(fullPath) + ".class";
		Path writePath = Paths.get(str);

		try {
			Files.write(writePath, bytes);
		} catch (IOException e) {
			System.out.println("Failed to write file: " + writePath + ".");

			return;
		}

		System.out.println("File written to: " + writePath + ".");
	}

	public String slashName() {
		// trim file extension, if any
		String str = stripExtension(Path.of(relPath));

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

    private String stripExtension(Path path) {
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex > 0) // Ensure dot is not the first character
            return path.getParent() != null
                ? path.getParent().resolve(fileName.substring(0, dotIndex)).toString()
                : fileName.substring(0, dotIndex);

        return path.toString(); // Return original if no extension
    }
    
	private String trimLeadingSlash(String str) {
		if (str.startsWith("/") || str.startsWith("\\"))
			return str.substring(1, str.length());
		else
			return str;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("relPath=%s\n", relPath));
		sb.append(String.format("slashName=%s\n", slashName()));
		sb.append(String.format("dotName=%s\n", dotName()));

		return sb.toString();
	}
}
