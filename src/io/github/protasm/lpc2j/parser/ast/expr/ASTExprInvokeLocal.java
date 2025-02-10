package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

import java.util.StringJoiner;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

public class ASTExprInvokeLocal extends ASTExpression {
    private LPCType lpcType;
    private final Integer slot;
    private final String methodName;
    private final ASTArguments args;

    public ASTExprInvokeLocal(int line, int slot, String methodName, ASTArguments args) {
	super(line);

	this.lpcType = null; // set in a subsequent pass
	this.slot = slot;
	this.methodName = methodName;
	this.args = args;
    }

    public void setLPCType(LPCType lpcType) {
	this.lpcType = lpcType;
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
    
    @Override
    public void typeInference(LPCType lpcType) {
	this.lpcType = lpcType;
    }

    @Override
    public void accept(MethodVisitor mv) {
	// 1. Load the target object from its local variable slot.
	mv.visitVarInsn(ALOAD, slot);

	// 2. Call getClass() on the target object.
	mv.visitMethodInsn(
		INVOKEVIRTUAL,
		"java/lang/Object",
		"getClass",
		"()Ljava/lang/Class;",
		false);

	// 3. Push the method name (as a constant) onto the stack.
	mv.visitLdcInsn(methodName);

	// 4. Build an array of Class objects representing the parameter types.
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

	// 5. Invoke Class.getMethod(String, Class[]) to get the Method object.
	mv.visitMethodInsn(
		INVOKEVIRTUAL,
		"java/lang/Class",
		"getMethod",
		"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
		false);

	// 6. Load the target object again (for the invoke call).
	mv.visitVarInsn(ALOAD, slot);

	// 7. Generate code to push the Object[] containing the actual argument values.
	args.accept(mv);

	// 8. Invoke Method.invoke(Object, Object[]); the result (an Object) is left on
	// the stack.
	mv.visitMethodInsn(
		INVOKEVIRTUAL,
		"java/lang/reflect/Method",
		"invoke",
		"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
		false);

	// Since the expression’s type is LPCMIXED, we leave the result as an Object.
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
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("%s%s(slot=%d, methodName=%s)", ASTNode.indent(), className(), slot, methodName));

	ASTNode.indentLvl++;

	sj.add(String.format("%s", args));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
