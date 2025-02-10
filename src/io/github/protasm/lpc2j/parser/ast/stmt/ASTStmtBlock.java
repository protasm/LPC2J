package io.github.protasm.lpc2j.parser.ast.stmt;

import java.util.List;
import java.util.StringJoiner;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

public class ASTStmtBlock extends ASTStatement {
    private final List<ASTStatement> statements;

    public ASTStmtBlock(int line, List<ASTStatement> statements) {
	super(line);

	this.statements = statements;
    }

    public List<ASTStatement> statements() {
	return statements;
    }
    
    @Override
    public void typeInference(LPCType lpcType) {
	for (ASTStatement stmt : statements)
	    stmt.typeInference(lpcType);
    }

    @Override
    public void accept(MethodVisitor mv) {
	for (ASTStatement statement : statements)
	    statement.accept(mv);
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("%s%s", ASTNode.indent(), className()));

	ASTNode.indentLvl++;

	if (statements.size() == 0)
	    sj.add(String.format("%s[No Statements]", ASTNode.indent()));

	for (ASTStatement stmt : statements)
	    sj.add(String.format("%s", stmt));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
