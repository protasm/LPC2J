package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTField;

import static org.objectweb.asm.Opcodes.*;

public class ASTExprFieldAccess extends ASTExpression {
    private ASTField field;

    public ASTExprFieldAccess(int line, ASTField field) {
	super(line);

	this.field = field;
    }

    public ASTField field() {
	return field;
    }

    @Override
    public LPCType lpcType() {
	return field.lpcType();
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, field.ownerName(), field.name(), field.descriptor());
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(field=%s)", className(), field.name()));

	return sb.toString();
    }
}
