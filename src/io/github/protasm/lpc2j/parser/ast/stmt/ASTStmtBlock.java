package io.github.protasm.lpc2j.parser.ast.stmt;

import java.util.List;

import org.objectweb.asm.MethodVisitor;

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
	public void toBytecode(MethodVisitor mv) {
		for (ASTStatement statement : statements)
			statement.toBytecode(mv);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s\n", className()));

		for (ASTStatement stmt : statements)
			sb.append(stmt);

		return sb.toString();
	}
}
