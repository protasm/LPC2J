package io.github.protasm.lpc2j.parser.ast.stmt;

import java.util.List;

import org.objectweb.asm.MethodVisitor;

import scanner.Token;

public class ASTStmtBlock extends ASTStatement {
    private final List<ASTStatement> statements;

    public ASTStmtBlock(Token startToken, List<ASTStatement> statements) {
	super(startToken);

	this.statements = statements;
    }

    public List<ASTStatement> statements() {
	return statements;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	for (ASTStatement statement : statements)
	    statement.toBytecode(mv);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append("ASTStmtBlock\n");

	for (ASTStatement stmt : statements)
	    sb.append(stmt).append("\n");

	return sb.toString().trim();
    }
}
