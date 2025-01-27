package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTField;

import static org.objectweb.asm.Opcodes.*;

public class ASTExprFieldStore extends ASTExpression {
    private String parentName;
    private ASTField field;
    private ASTExpression value;

    public ASTExprFieldStore(int line, String parentName, ASTField field, ASTExpression value) {
	super(line);

	this.parentName = parentName;
	this.field = field;
	this.value = value;
    }

    public String parentName() {
	return parentName;
    }

    public ASTField field() {
	return field;
    }

    public ASTExpression value() {
	return value;
    }

    @Override
    public LPCType lpcType() {
	return field.lpcType();
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	mv.visitVarInsn(ALOAD, 0);

	value.toBytecode(mv);

	mv.visitFieldInsn(PUTFIELD, parentName, field.name(), field.descriptor());
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(field=%s, value=%s)", className(), field.name(), value));

	return sb.toString();
    }
}
