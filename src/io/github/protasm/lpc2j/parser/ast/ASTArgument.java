package io.github.protasm.lpc2j.parser.ast;

import java.util.StringJoiner;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

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
    public void toBytecode(MethodVisitor mv) {
	expression.toBytecode(mv);
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("%s%s", ASTNode.indent(), className()));

	ASTNode.indentLvl++;

	sj.add(String.format("%s", expression));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
