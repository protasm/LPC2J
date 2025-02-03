package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;

public class ASTExprLocalMethodInvoke extends ASTExpression {
	private final Integer localSlot;
	private final String methodName;
	private final ASTArguments args;

	public ASTExprLocalMethodInvoke(int line, int localSlot, String methodName, ASTArguments args) {
		super(line);

		this.localSlot = localSlot;
		this.methodName = methodName;
		this.args = args;
	}

	public Integer localSlot() {
		return localSlot;
	}

	public String methodName() {
		return methodName;
	}

	public ASTArguments args() {
		return args;
	}

	@Override
	public LPCType lpcType() {
		// TODO Auto-generated method stub
		return LPCType.LPCMIXED;
	}

	@Override
	public void toBytecode(MethodVisitor mv) {
		// Step 1: Load the object from the local variable slot
		mv.visitVarInsn(Opcodes.ALOAD, localSlot); // Load obj from local variable slot

		// Step 2: Push the method name (e.g., "bar")
		mv.visitLdcInsn(methodName); // Push method name as a string constant

		// Step 3: Box up the method arguments into an array
		args.toBytecode(mv);

		// Step 4: Invoke `dispatch(methodName, Object...)`
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "io/github/protasm/lpc2j/runtime/LPCObject", // Class
				"dispatch", // Method
				"(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;", // Descriptor
				false // Not an interface
		);

		// Step 5: Handle the return value (if needed)
//        if (expectsReturnValue) {
//            unboxIfPrimitive(mv, returnType);
//        } else {
//            mv.visitInsn(Opcodes.POP); // Discard return value if void
//        }
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s", className()));

		return sb.toString();
	}
}
