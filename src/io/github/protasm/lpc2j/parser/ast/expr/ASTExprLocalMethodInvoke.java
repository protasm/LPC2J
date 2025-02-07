package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;

public class ASTExprLocalMethodInvoke extends ASTExpression {
	private final Integer slot;
	private final String methodName;
	private final ASTArguments args;

	public ASTExprLocalMethodInvoke(int line, int slot, String methodName, ASTArguments args) {
		super(line);

		this.slot = slot;
		this.methodName = methodName;
		this.args = args;
	}

	public Integer slot() {
		return slot;
	}

	public String methodName() {
		return methodName;
	}

	public ASTArguments args() {
		return args;
	}

	@Override
	public LPCType lpcType() {
		return LPCType.LPCMIXED;
	}

	@Override
	public void toBytecode(MethodVisitor mv) {
	    // Load the target object from its slot
	    mv.visitVarInsn(ALOAD, slot);

	    // Get the Class of the object
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);

	    // Push method name
	    mv.visitLdcInsn(methodName);

	 // Generate and push the Class<?>[] for method signature
	    mv.visitLdcInsn(args.size()); // Array length
	    mv.visitTypeInsn(ANEWARRAY, "java/lang/Class"); // new Class<?>[size]

	    for (int i = 0; i < args.size(); i++) {
	        mv.visitInsn(DUP); // Duplicate array reference
	        mv.visitLdcInsn(i); // Push index

	        // Get LPCType of the argument expression
	        LPCType argType = args.get(i).expr().lpcType();

	        // Push corresponding Java Class type
	        pushLPCTypeClass(mv, argType);

	        mv.visitInsn(AASTORE); // Store into array
	    }

	    // Call getMethod(methodName, argumentTypes)
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
	            "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);

	    // Load the target object again for invocation
	    mv.visitVarInsn(ALOAD, slot);

	    // Generate and push the Object[] containing actual argument values
	    args.toBytecode(mv);

	 // Call Method.invoke(obj, args)
	    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
	            "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);

	    // Cast result to String
	    mv.visitTypeInsn(CHECKCAST, "java/lang/String");

	    // Return the String result
	    mv.visitInsn(ARETURN);
	}
	private void pushLPCTypeClass(MethodVisitor mv, LPCType type) {
	    switch (type.jType()) {
	        case JINT:
	            mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
	            break;
	        case JFLOAT:
	            mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
	            break;
	        case JBOOLEAN:
	            mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
	            break;
	        case JSTRING:
	            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
	            break;
	        case JOBJECT:
	            mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
	            break;
	        default:
	            throw new IllegalArgumentException("Unsupported LPCType: " + type);
	    }
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s", className()));

		return sb.toString();
	}
}
