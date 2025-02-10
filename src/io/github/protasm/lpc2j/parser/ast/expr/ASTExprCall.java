package io.github.protasm.lpc2j.parser.ast.expr;

import java.util.StringJoiner;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

public class ASTExprCall extends ASTExpression {
    private ASTMethod method;
    private ASTArguments arguments;

    public ASTExprCall(int line, ASTMethod method, ASTArguments arguments) {
	super(line);

	this.method = method;
	this.arguments = arguments;
    }

    public ASTMethod method() {
	return method;
    }

    public ASTArguments arguments() {
	return arguments;
    }

    @Override
    public LPCType lpcType() {
	return method.symbol().lpcType();
    }

    @Override
    public void accept(MethodVisitor mv) {
	mv.visitVarInsn(Opcodes.ALOAD, 0);

	arguments.accept(mv);

	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, method.ownerName(), method.symbol().name(), method.descriptor(),
		false);

	// Pop if the method returns a value but it's unused
//		if (!method.lpcReturnType().equals("V"))
//			mv.visitInsn(Opcodes.POP);
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("%s%s", ASTNode.indent(), className()));

	ASTNode.indentLvl++;

	sj.add(String.format("%s%s", ASTNode.indent(), method.symbol()));
	sj.add(String.format("%s", arguments));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
