package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.Local;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import java.util.StringJoiner;

public class ASTExprLocalAccess extends ASTExpression {
    private final Local local;

    public ASTExprLocalAccess(int line, Local local) {
	super(line);

	this.local = local;
    }

    public Local local() {
	return local;
    }

    @Override
    public LPCType lpcType() {
	return local.symbol().lpcType();
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	switch (local.symbol().lpcType()) {
	case LPCINT:
	case LPCSTATUS:
	    mv.visitVarInsn(ILOAD, local.slot());
	    break;
	case LPCSTRING:
	case LPCOBJECT:
	    mv.visitVarInsn(ALOAD, local.slot());
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

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
