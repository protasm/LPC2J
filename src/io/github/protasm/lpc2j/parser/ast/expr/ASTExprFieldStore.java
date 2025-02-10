package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTNode;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

import static org.objectweb.asm.Opcodes.*;

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
	public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(PrintVisitor visitor) {
		visitor.visit(this);
	}
}
