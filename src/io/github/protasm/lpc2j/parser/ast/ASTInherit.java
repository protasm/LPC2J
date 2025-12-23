package io.github.protasm.lpc2j.parser.ast;

public final class ASTInherit extends ASTNode {
    private final String path;

    public ASTInherit(int line, String path) {
        super(line);
        this.path = path;
    }

    public String path() {
        return path;
    }
}
