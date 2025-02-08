package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.Local;

import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class ASTExprLocalAccess extends ASTExpression {
    private final int line;
    private final Local local;

    public ASTExprLocalAccess(int line, Local local) {
	super(line);

	this.line = line;
	this.local = local;
    }

    public int line() {
	return line;
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
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s\n", className()));

	return sb.toString();
    }
}
