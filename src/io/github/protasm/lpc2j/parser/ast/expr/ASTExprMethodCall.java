package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;

public class ASTExprMethodCall extends ASTExpression {
	private ASTMethod method;
	private ASTArguments arguments;

	public ASTExprMethodCall(int line, ASTMethod method, ASTArguments arguments) {
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
		return method.lpcReturnType();
	}

	@Override
	public void toBytecode(MethodVisitor mv) {
		mv.visitVarInsn(Opcodes.ALOAD, 0);

		arguments.toBytecode(mv);

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, method.ownerName(), method.name(), method.descriptor(), false);

		// Pop if the method returns a value but it's unused
//		if (!method.lpcReturnType().equals("V"))
//			mv.visitInsn(Opcodes.POP);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s(method=%s, args=%s", className(), method, arguments));

		return sb.toString();
	}
}
