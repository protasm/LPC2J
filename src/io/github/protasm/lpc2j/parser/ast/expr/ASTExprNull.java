package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;

public class ASTExprNull extends ASTExpression {
	public ASTExprNull(int line) {
		super(line);
	}

	@Override
	public LPCType lpcType() {
		return LPCType.LPCNULL;
	}

	@Override
	public void accept(MethodVisitor visitor) {
		// TODO Auto-generated method stub

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
