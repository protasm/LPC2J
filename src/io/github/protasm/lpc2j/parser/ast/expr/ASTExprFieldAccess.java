package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

import static org.objectweb.asm.Opcodes.*;

import java.util.StringJoiner;

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
	return field.symbol().lpcType();
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, field.ownerName(), field.symbol().name(), field.descriptor());
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("%s%s", ASTNode.indent(), className()));

	ASTNode.indentLvl++;

	sj.add(String.format("%s", field));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
