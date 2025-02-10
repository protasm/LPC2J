package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.Local;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

import static org.objectweb.asm.Opcodes.*;

import java.util.StringJoiner;

public class ASTExprLocalStore extends ASTExpression {
    private Local local;
    private ASTExpression value;

    public ASTExprLocalStore(int line, Local local, ASTExpression value) {
	super(line);

	this.local = local;
	this.value = value;
    }

    public Local local() {
	return local;
    }

    public ASTExpression value() {
	return value;
    }

    @Override
    public LPCType lpcType() {
	return local.symbol().lpcType();
    }

    @Override
    public void accept(MethodVisitor mv) {
	value.accept(mv);

	switch (local.symbol().lpcType()) {
	case LPCINT:
	case LPCSTATUS:
	    mv.visitVarInsn(ISTORE, local.slot());
	    break;
	case LPCSTRING:
	case LPCOBJECT:
	    mv.visitVarInsn(ASTORE, local.slot());
	    break;
	default:
	    throw new IllegalStateException("Unsupported type: " + local.symbol().lpcType());
	}
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("%s%s", ASTNode.indent(), className()));

	ASTNode.indentLvl++;

	sj.add(String.format("%s", local));
	sj.add(String.format("%s", value));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
