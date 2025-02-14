package io.github.protasm.lpc2j.parser.ast.stmt;

import java.util.Iterator;
import java.util.List;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTStmtBlock extends ASTStatement implements Iterable<ASTStatement> {
    private final List<ASTStatement> statements;

    public ASTStmtBlock(int line, List<ASTStatement> statements) {
	super(line);

	this.statements = statements;
    }

    public int size() {
	return statements.size();
    }

    @Override
    public Iterator<ASTStatement> iterator() {
	return statements.iterator();
    }

    @Override
    public void accept(Compiler visitor) {
	visitor.visit(this);
    }

    @Override
    public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
	visitor.visit(this, lpcType);
    }

    @Override
    public void accept(PrintVisitor visitor) {
	visitor.visit(this);
    }
}
