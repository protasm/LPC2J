package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprLiteralFalse extends ASTExpression {
	public ASTExprLiteralFalse(int line) {
		super(line);
	}

	@Override
	public LPCType lpcType() {
		return LPCType.LPCSTATUS;
	}

	@Override
	public void accept(Compiler visitor) {
		visitor.visit(this);
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
