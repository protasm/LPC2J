package io.github.protasm.lpc2j.parser.ast.stmt;

import java.util.List;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;

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
	public void accept(MethodVisitor mv) {
		for (ASTStatement statement : statements)
			statement.accept(mv);
	}

	@Override
	public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(PrintVisitor visitor) {
		visitor.visit(this);
	}
}
