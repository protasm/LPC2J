package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprLocalAccess extends ASTExpression {
	private final ASTLocal local;

	public ASTExprLocalAccess(int line, ASTLocal local) {
		super(line);

		this.local = local;
	}

	public ASTLocal local() {
		return local;
	}

	@Override
	public LPCType lpcType() {
		return local.symbol().lpcType();
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
