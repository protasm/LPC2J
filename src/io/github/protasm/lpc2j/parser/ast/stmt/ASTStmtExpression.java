package io.github.protasm.lpc2j.parser.ast.stmt;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;

public class ASTStmtExpression extends ASTStatement {
	private final ASTExpression expression;

	public ASTStmtExpression(int line, ASTExpression expression) {
		super(line);

		this.expression = expression;
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
