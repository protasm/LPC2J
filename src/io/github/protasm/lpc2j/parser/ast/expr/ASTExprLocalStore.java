package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

import static org.objectweb.asm.Opcodes.*;

public class ASTExprLocalStore extends ASTExpression {
	private ASTLocal local;
	private ASTExpression value;

	public ASTExprLocalStore(int line, ASTLocal local, ASTExpression value) {
		super(line);

		this.local = local;
		this.value = value;
	}

	public ASTLocal local() {
		return local;
	}

	public ASTExpression value() {
		return value;
	}

	@Override
	public LPCType lpcType() {
		return local.symbol().lpcType();
	}

	@Override
	public void accept(MethodVisitor mv) {
		value.accept(mv);

		switch (local.symbol().lpcType()) {
		case LPCINT:
		case LPCSTATUS:
			mv.visitVarInsn(ISTORE, local.slot());
		break;
		case LPCSTRING:
		case LPCOBJECT:
			mv.visitVarInsn(ASTORE, local.slot());
		break;
		default:
			throw new IllegalStateException("Unsupported type: " + local.symbol().lpcType());
		}
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
