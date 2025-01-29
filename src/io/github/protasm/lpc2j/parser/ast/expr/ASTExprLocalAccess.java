package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.LPCType;
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
	return local.lpcType();
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	mv.visitVarInsn(ILOAD, local.slot());
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(local=%s)", className(), local));

	return sb.toString();
    }
}
