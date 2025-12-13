package io.github.protasm.lpc2j;

import java.io.File;
import java.nio.file.Path;

import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.token.TokenList;

/**
 * Representation of a source file that flows through scan/parse/compile
 * phases. Holds the relative path from a VirtualFileServer base along with the
 * intermediate artifacts produced by each phase.
 */
public class FSSourceFile {
    private final Path relativePath;
    private String source;
    private TokenList tokens;
    private ASTObject astObject;
    private byte[] bytes;

    public FSSourceFile(Path relativePath) {
        if (relativePath == null)
            throw new IllegalArgumentException("relativePath");
        if (relativePath.isAbsolute())
            throw new IllegalArgumentException("relativePath must be relative to base");

        this.relativePath = relativePath.normalize();
    }

    public Path relativePath() {
        return relativePath;
    }

    public String slashName() {
        return relativePath.toString().replace(File.separatorChar, '/');
    }

    public String source() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public TokenList tokens() {
        return tokens;
    }

    public void setTokens(TokenList tokens) {
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
}
