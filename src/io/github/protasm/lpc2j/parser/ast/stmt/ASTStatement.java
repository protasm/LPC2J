package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.ASTNode;

public abstract class ASTStatement extends ASTNode {
    public ASTStatement(int line) {
        super(line);
    }
}
