package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

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

	// Step 3: Load the method name.
	mv.visitLdcInsn(methodName);

	// Step 4: Load the method parameter types (Class[]).
	args.paramTypes(mv);

	// Step 5: Invoke Class.getMethod(String, Class[]) to get the Method object.
	mv.visitMethodInsn(
		INVOKEVIRTUAL,
		"java/lang/Class",
		"getMethod",
		"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
		false);

	// Step 6: Load the target object again (for the invoke call).
	mv.visitVarInsn(ALOAD, slot);

	// Step 7: Load the actual argument values (Object[]).
	args.accept(mv);

	// Step 8: Invoke Method.invoke(Object, Object[]), returning Object.
	mv.visitMethodInsn(
		INVOKEVIRTUAL,
		"java/lang/reflect/Method",
		"invoke",
		"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
		false);

	// Step 9: Unbox/cast return value.
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

    @Override
    public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
	visitor.visit(this, lpcType);
    }

    @Override
    public void accept(PrintVisitor visitor) {
	visitor.visit(this);
    }
}
