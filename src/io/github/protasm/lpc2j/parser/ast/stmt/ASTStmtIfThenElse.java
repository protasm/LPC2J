package io.github.protasm.lpc2j.parser.ast.stmt;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.ASTNode;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

import org.objectweb.asm.Label;
import static org.objectweb.asm.Opcodes.*;

import java.util.StringJoiner;

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
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("%s%s", ASTNode.indent(), className()));

	ASTNode.indentLvl++;

	sj.add(String.format("%s[IF]", ASTNode.indent()));

	ASTNode.indentLvl++;

	sj.add(String.format("%s", condition));

	ASTNode.indentLvl--;

	sj.add(String.format("%s[THEN]", ASTNode.indent()));

	ASTNode.indentLvl++;

	sj.add(String.format("%s", thenBranch));

	ASTNode.indentLvl--;

	if (elseBranch != null) {
	    sj.add(String.format("%s[ELSE]", ASTNode.indent()));

	    ASTNode.indentLvl++;

	    sj.add(String.format("%s", elseBranch));

	    ASTNode.indentLvl--;

	} else
	    sj.add(String.format("%s[No Else Condition]", ASTNode.indent()));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
