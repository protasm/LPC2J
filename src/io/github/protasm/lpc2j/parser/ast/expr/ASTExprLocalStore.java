package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprLocalStore extends ASTExpression {
	private final ASTLocal local;
	private final ASTExpression value;

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
