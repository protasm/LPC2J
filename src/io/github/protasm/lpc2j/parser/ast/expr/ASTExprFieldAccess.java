package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

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
	return field.symbol().lpcType();
    }

    @Override
    public void accept(MethodVisitor mv) {
	mv.visitVarInsn(ALOAD, 0);
	mv.visitFieldInsn(GETFIELD, field.ownerName(), field.symbol().name(), field.descriptor());
    }

    @Override
    public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
	visitor.visit(this, lpcType);
    }

    @Override
    public void accept(PrintVisitor visitor) {
	visitor.visit(this);
    }
}
