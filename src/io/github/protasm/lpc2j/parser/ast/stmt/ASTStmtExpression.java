package io.github.protasm.lpc2j.parser.ast.stmt;

import java.util.StringJoiner;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTNode;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

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
    public void typeInference(LPCType lpcType) {
	expression.typeInference(lpcType);
    }

    @Override
    public void accept(MethodVisitor mv) {
	expression.accept(mv);
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
