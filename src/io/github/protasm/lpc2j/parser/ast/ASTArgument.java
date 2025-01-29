package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class ASTArgument extends ASTNode {
    private final ASTExpression expr;

    public ASTArgument(int line, ASTExpression expr) {
	super(line);

	this.expr = expr;
    }

    public ASTExpression expr() {
	return expr;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	expr.toBytecode(mv);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(expr=%s)\n", className(), expr));

	return sb.toString();
    }
}
