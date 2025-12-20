package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ASTStmtBlock extends ASTStatement implements Iterable<ASTStatement> {
    private final List<ASTStatement> statements;

    public ASTStmtBlock(int line) {
        super(line);
        this.statements = new ArrayList<>();
    }

    public ASTStmtBlock(int line, List<ASTStatement> statements) {
        super(line);
        this.statements = new ArrayList<>(statements);
    }

    public void add(ASTStatement stmt) {
        statements.add(stmt);
    }

    public int size() {
        return statements.size();
    }

    public List<ASTStatement> statements() {
        return statements;
    }

    @Override
    public Iterator<ASTStatement> iterator() {
        return statements.iterator();
    }
}
