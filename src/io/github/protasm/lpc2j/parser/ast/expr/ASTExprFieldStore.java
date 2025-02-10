package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

import static org.objectweb.asm.Opcodes.*;

import java.util.StringJoiner;

public class ASTExprFieldStore extends ASTExpression {
    private ASTField field;
    private ASTExpression value;

    public ASTExprFieldStore(int line, ASTField field, ASTExpression value) {
	super(line);

	this.field = field;
	this.value = value;
    }

    public ASTField field() {
	return field;
    }

    public ASTExpression value() {
	return value;
    }

    @Override
    public LPCType lpcType() {
	return field.symbol().lpcType();
    }

    @Override
    public void accept(MethodVisitor mv) {
	mv.visitVarInsn(ALOAD, 0);

	value.accept(mv);

	mv.visitFieldInsn(PUTFIELD, field.ownerName(), field.symbol().name(), field.symbol().descriptor());
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("%s%s", ASTNode.indent(), className()));

	ASTNode.indentLvl++;

	sj.add(String.format("%s", field));
	sj.add(String.format("%s", value));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
