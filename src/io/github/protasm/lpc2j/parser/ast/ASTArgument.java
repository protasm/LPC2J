package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;

public class ASTArgument extends ASTNode {
	private final ASTExpression expression;

	public ASTArgument(int line, ASTExpression expr) {
		super(line);

		this.expression = expr;
	}

	public ASTExpression expression() {
		return expression;
	}

	@Override
	public void accept(MethodVisitor mv) {
		expression.accept(mv);
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
