package io.github.protasm.lpc2j.parser.ast.stmt;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;

import org.objectweb.asm.Label;
import static org.objectweb.asm.Opcodes.*;

public class ASTStmtIfThenElse extends ASTStatement {
	private final ASTExpression condition;
	private final ASTStatement thenBranch;
	private final ASTStatement elseBranch; // Nullable

	public ASTStmtIfThenElse(int line, ASTExpression condition, ASTStatement thenBranch, ASTStatement elseBranch) {
		super(line);

		this.condition = condition;
		this.thenBranch = thenBranch;
		this.elseBranch = elseBranch;
	}

	public ASTExpression condition() {
		return condition;
	}

	public ASTStatement thenBranch() {
		return thenBranch;
	}

	public ASTStatement elseBranch() {
		return elseBranch;
	}

	@Override
	public void accept(MethodVisitor mv) {
		Label elseLabel = new Label();
		Label endLabel = new Label();

		// Generate bytecode for condition
		condition.accept(mv);

		// If condition is false, jump to else (or end if no else)
		mv.visitJumpInsn(IFEQ, elseBranch != null ? elseLabel : endLabel);

		// Generate bytecode for then-branch
		thenBranch.accept(mv);

		// Skip else-branch (if it exists)
		if (elseBranch != null) {
			mv.visitJumpInsn(GOTO, endLabel);

			mv.visitLabel(elseLabel);

			elseBranch.accept(mv);
		}

		// End label
		mv.visitLabel(endLabel);
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
