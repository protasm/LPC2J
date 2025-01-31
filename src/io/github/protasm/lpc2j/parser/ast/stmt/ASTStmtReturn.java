package io.github.protasm.lpc2j.parser.ast.stmt;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class ASTStmtReturn extends ASTStatement {
    private final ASTExpression value; // return value, if any

    public ASTStmtReturn(int line, ASTExpression value) {
	super(line);

	this.value = value;
    }

    public ASTExpression value() {
	return value;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	if (value == null) {
	    mv.visitInsn(Opcodes.RETURN);

	    return;
	}

	value.toBytecode(mv);

	switch (value.lpcType()) {
	case LPCINT:
	    mv.visitInsn(Opcodes.IRETURN);
	    break;
	case LPCSTRING:
	case LPCOBJECT:
	    mv.visitInsn(Opcodes.ARETURN);
	    break;
	default:
	    throw new UnsupportedOperationException("Unsupported return value type: " + value.lpcType());
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s\n", className()));

	return sb.toString();
    }
}
