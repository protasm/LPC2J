package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;

public class ASTExprLiteralTrue extends ASTExpression {
	public ASTExprLiteralTrue(int line) {
		super(line);
	}

	@Override
	public LPCType lpcType() {
		return LPCType.LPCSTATUS;
	}

	@Override
	public void accept(MethodVisitor mv) {
		mv.visitInsn(Opcodes.ICONST_1);
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
