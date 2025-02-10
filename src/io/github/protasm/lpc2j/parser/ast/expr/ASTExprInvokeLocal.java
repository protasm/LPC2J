package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprInvokeLocal extends ASTExpression {
    private LPCType lpcType;
    private final Integer slot;
    private final String methodName;
    private final ASTArguments args;

    public ASTExprInvokeLocal(int line, int slot, String methodName, ASTArguments args) {
	super(line);

	this.lpcType = null; // set in type inference pass
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
	return lpcType;
    }

    public void setLPCType(LPCType lpcType) {
	// called during Parser's type inference pass
	this.lpcType = lpcType;
    }

    @Override
    public void accept(MethodVisitor mv) {
        // Step 1: Load the target object from its local variable slot.
        mv.visitVarInsn(ALOAD, slot);

        // Step 2: Call getClass() on the target object.
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/Object",
                "getClass",
                "()Ljava/lang/Class;",
                false);

        // Step 3: Push the method name (as a constant) onto the stack.
        mv.visitLdcInsn(methodName);

        // Step 4: Build an array of Class objects representing the parameter types.
        int numArgs = args.size();
        mv.visitLdcInsn(numArgs);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

        for (int i = 0; i < numArgs; i++) {
            mv.visitInsn(DUP); // Duplicate array reference.
            mv.visitLdcInsn(i); // Push array index.

            // Get the LPC type for the i-th argument.
            LPCType argType = args.get(i).expression().lpcType();

            pushLPCTypeClass(mv, argType); // Push the corresponding Java Class object.
            mv.visitInsn(AASTORE); // Store it in the array.
        }

        // Step 5: Invoke Class.getMethod(String, Class[]) to get the Method object.
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/Class",
                "getMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                false);

        // Step 6: Load the target object again (for the invoke call).
        mv.visitVarInsn(ALOAD, slot);

        // Step 7: Generate code to push the Object[] containing the actual argument values.
        args.accept(mv);

        // Step 8: Invoke Method.invoke(Object, Object[]); the result (an Object) is left on the stack.
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/reflect/Method",
                "invoke",
                "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                false);

        // Step 9: unbox/cast return value
        unboxResult(mv);
    }

    /**
     * Helper method to convert the Object returned by Method.invoke to the
     * expected type based on the inferred LPCType.
     */
    private void unboxResult(MethodVisitor mv) {
        if (lpcType != null) {
            switch (lpcType.jType()) {
                case JINT:
                    // Cast to Integer and unbox to int.
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                    mv.visitMethodInsn(
                            INVOKEVIRTUAL,
                            "java/lang/Integer",
                            "intValue",
                            "()I",
                            false);
                    break;
                case JFLOAT:
                    // Cast to Float and unbox to float.
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                    mv.visitMethodInsn(
                            INVOKEVIRTUAL,
                            "java/lang/Float",
                            "floatValue",
                            "()F",
                            false);
                    break;
                case JBOOLEAN:
                    // Cast to Boolean and unbox to boolean.
                    mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                    mv.visitMethodInsn(
                            INVOKEVIRTUAL,
                            "java/lang/Boolean",
                            "booleanValue",
                            "()Z",
                            false);
                    break;
                case JSTRING:
                    // Cast to String.
                    mv.visitTypeInsn(CHECKCAST, "java/lang/String");
                    break;
                default:
                    // For LPCMIXED or other types, leave the result as Object,
                    // or add an appropriate cast if necessary.
                    break;
            }
        }
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
	default:
	    // For LPCMIXED or other types, default to Object.
	    mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
	    break;
	}
    }

    @Override
    public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
	visitor.visit(this, lpcType);
    }

    @Override
    public void accept(PrintVisitor visitor) {
	visitor.visit(this);
    }
}
