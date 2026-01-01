package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class ASTStmtBlock extends ASTStatement implements Iterable<ASTStatement> {
    private final List<ASTStatement> statements;
    private final List<BlockLocalDeclaration> localDeclarations;

    public ASTStmtBlock(int line) {
        super(line);
        this.statements = new ArrayList<>();
        this.localDeclarations = new ArrayList<>();
    }

    public ASTStmtBlock(int line, List<ASTStatement> statements) {
        super(line);
        this.statements = new ArrayList<>(statements);
        this.localDeclarations = new ArrayList<>();
    }

    public ASTStmtBlock(int line, List<ASTStatement> statements, List<BlockLocalDeclaration> localDeclarations) {
        super(line);
        this.statements = new ArrayList<>(statements);
        this.localDeclarations = new ArrayList<>(localDeclarations);
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

    public List<BlockLocalDeclaration> localDeclarations() {
        return Collections.unmodifiableList(localDeclarations);
    }

    public void addLocalDeclaration(int statementIndex, List<ASTLocal> locals) {
        if (locals == null || locals.isEmpty())
            return;

        localDeclarations.add(new BlockLocalDeclaration(statementIndex, locals));
    }

    @Override
    public Iterator<ASTStatement> iterator() {
        return statements.iterator();
    }

    public static final class BlockLocalDeclaration {
        private final int statementIndex;
        private final List<ASTLocal> locals;

        public BlockLocalDeclaration(int statementIndex, List<ASTLocal> locals) {
            this.statementIndex = statementIndex;
            this.locals = new ArrayList<>(locals);
        }

        public int statementIndex() {
            return statementIndex;
        }

        public List<ASTLocal> locals() {
            return Collections.unmodifiableList(locals);
        }
    }
}
