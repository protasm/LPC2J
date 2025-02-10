package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTNode;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

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
		return "call";
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
