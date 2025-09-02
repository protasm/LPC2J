package io.github.protasm.lpc2j.compiler;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INEG;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V23;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTFields;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTMethods;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralTrue;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprNull;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.JType;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;

public class Compiler {
	private final String defaultParentName;
	private final ClassWriter cw;
	private MethodVisitor mv; // current method

	public Compiler(String defaultParentName) {
		this.defaultParentName = defaultParentName;

		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
	}

	public byte[] compile(ASTObject astObject) {
		if (astObject == null)
			return null;

		astObject.accept(this);

		return this.cw.toByteArray();
	}

	public void visit(ASTArgument arg) {
		arg.expression().accept(this);

		LPCType type = arg.expression().lpcType();

		if (type == null) // Without type information, assume no boxing is needed.
			return;

		// box primitive value, if needed
		switch (type.jType()) {
		case JINT: // Integer.valueOf(int)
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			break;
		case JFLOAT: // Float.valueOf(float)
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
			break;
		case JBOOLEAN: // Boolean.valueOf(boolean)
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
			break;
		default: // For non-primitive types (or types that don't need boxing).
			break;
		}
	}

	public void visit(ASTArguments args) {
		pushInt(args.size()); // Push array length

		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // Create Object[]

		for (int i = 0; i < args.size(); i++) {
			mv.visitInsn(DUP); // Duplicate array reference

			pushInt(i); // Push index

			args.nodes().get(i).accept(this); // Push argument value

			mv.visitInsn(AASTORE); // Store argument into array
		}
	}

	public void visit(ASTExprCallMethod expr) {
		ASTMethod method = expr.method();
		ASTArguments args = expr.arguments();

		// Load "this" reference
		mv.visitVarInsn(Opcodes.ALOAD, 0);

		// Push each argument value individually on the stack. The invoked
		// method expects its arguments directly on the operand stack
		// according to its descriptor (e.g. an int for "(I)I").
		for (ASTArgument arg : args.nodes())
			arg.expression().accept(this);

		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, method.ownerName(), method.symbol().name(), method.descriptor(),
				false);

		// Pop if the method returns a value but it's unused
//		if (!method.lpcReturnType().equals("V"))
//			mv.visitInsn(Opcodes.POP);
	}

	public void visit(ASTExprCallEfun expr) {
		Efun efun = expr.efun();
		ASTArguments args = expr.arguments();

		mv.visitLdcInsn(efun.symbol().name());

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "io/github/protasm/lpc2j/efun/EfunRegistry", "lookup",
				"(Ljava/lang/String;)Lio/github/protasm/lpc2j/efun/Efun;", false);

		// Null-check to avoid null-pointer error
		var ok = new org.objectweb.asm.Label();

		mv.visitInsn(Opcodes.DUP);
		mv.visitJumpInsn(Opcodes.IFNONNULL, ok);
		mv.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
		mv.visitInsn(Opcodes.DUP);
		mv.visitLdcInsn("Unknown efun: '" + efun.symbol().name() + "'");

		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V",
				false);

		mv.visitInsn(Opcodes.ATHROW);
		mv.visitLabel(ok);

		// For non-static invocation, bundle arguments in an Object[] array
		args.accept(this);

		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "io/github/protasm/lpc2j/efun/Efun", "invoke",
				"([Ljava/lang/Object;)Ljava/lang/Object;", true);
	}

	public void visit(ASTExprFieldAccess expr) {
		ASTField field = expr.field();

		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, field.ownerName(), field.symbol().name(), field.descriptor());
	}

	public void visit(ASTExprFieldStore expr) {
		ASTField field = expr.field();
		ASTExpression value = expr.value();

		mv.visitVarInsn(ALOAD, 0);

		value.accept(this);

		mv.visitFieldInsn(PUTFIELD, field.ownerName(), field.symbol().name(), field.symbol().descriptor());
	}

	public void visit(ASTExprInvokeLocal expr) {
		ASTArguments args = expr.args();

		// Load target object
		invokeLoadLocalObj(expr.slot());

		// Call getClass() on target object
		invokeGetClass();

		// Load method name
		mv.visitLdcInsn(expr.methodName());

		// Load method parameter types (Class[])
		invokeParamTypes(args);

		// Invoke Class.getMethod(String, Class[]) to get Method object
		invokeGetMethod();

		// Reload target object (for invoke call)
		invokeLoadLocalObj(expr.slot());

		// Load actual argument values (Object[])
		args.accept(this);

		// Invoke Method.invoke(Object, Object[]), returning Object
		invokeMethodInvoke();

		// Unbox/cast return value
		invokeReturnValue(expr.lpcType());
	}

	public void visit(ASTExprLiteralFalse expr) {
		mv.visitInsn(Opcodes.ICONST_0);
	}

	public void visit(ASTExprLiteralInteger expr) {
		Integer value = expr.value();

		pushInt(value);
	}

	public void visit(ASTExprLiteralString expr) {
		mv.visitLdcInsn(expr.value());
	}

	public void visit(ASTExprLiteralTrue expr) {
		mv.visitInsn(Opcodes.ICONST_1);
	}

	public void visit(ASTExprLocalAccess expr) {
		ASTLocal local = expr.local();

		switch (local.symbol().lpcType()) {
		case LPCINT:
		case LPCSTATUS:
			mv.visitVarInsn(ILOAD, local.slot());
			break;
		case LPCSTRING:
		case LPCOBJECT:
			mv.visitVarInsn(ALOAD, local.slot());
			break;
		default:
			throw new IllegalStateException("Unsupported type: " + local.symbol().lpcType());
		}
	}

	public void visit(ASTExprLocalStore expr) {
		ASTLocal local = expr.local();
		ASTExpression value = expr.value();

		value.accept(this);

		switch (local.symbol().lpcType()) {
		case LPCINT:
		case LPCSTATUS:
			mv.visitVarInsn(ISTORE, local.slot());
			break;
		case LPCSTRING:
		case LPCOBJECT:
			mv.visitVarInsn(ASTORE, local.slot());
			break;
		default:
			throw new IllegalStateException("Unsupported type: " + local.symbol().lpcType());
		}
	}

	public void visit(ASTExprNull expr) {
	}

	public void visit(ASTExprOpBinary expr) {
		ASTExpression left = expr.left();
		ASTExpression right = expr.right();
		BinaryOpType operator = expr.operator();

		left.accept(this);
		right.accept(this);

		switch (operator) {
		case BOP_ADD:
		case BOP_SUB:
		case BOP_MULT:
		case BOP_DIV:
			mv.visitInsn(operator.opcode());
			break;
		case BOP_GT:
		case BOP_GE:
		case BOP_LT:
		case BOP_LE:
		case BOP_EQ:
			Label labelTrue = new Label();
			Label labelEnd = new Label();

			// Compare left vs right
			mv.visitJumpInsn(operator.opcode(), labelTrue);

			// False case: Push 0 (false)
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, labelEnd);

			// True case: Push 1 (true)
			mv.visitLabel(labelTrue);
			mv.visitInsn(ICONST_1);

			// End label
			mv.visitLabel(labelEnd);
			break;
		default:
			throw new UnsupportedOperationException("Unsupported operator: " + operator);
		}
	}

	public void visit(ASTExprOpUnary expr) {
		ASTExpression right = expr.right();
		UnaryOpType operator = expr.operator();

		right.accept(this);

		switch (operator) {
		case UOP_NEGATE: // Unary minus (-)
			mv.visitInsn(INEG);
			break;
		case UOP_NOT: // Logical NOT (!)
			Label trueLabel = new Label();
			Label endLabel = new Label();

			// Jump if operand is false (0)
			mv.visitJumpInsn(IFEQ, trueLabel);

			// Operand is true, push 0 (false)
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, endLabel);

			// Operand is false, push 1 (true)
			mv.visitLabel(trueLabel);
			mv.visitInsn(ICONST_1);

			// End
			mv.visitLabel(endLabel);

			break;
		}
	}

	public void visit(ASTField field) {
		FieldVisitor fv = cw.visitField(ACC_PRIVATE, field.symbol().name(), field.descriptor(), null, null);

		// initializer bytecode deferred to constructor

		fv.visitEnd();
	}

	public void visit(ASTFields fields) {
		for (ASTField field : fields)
			field.accept(this);
	}

	public void visit(ASTLocal local) {
		// TODO Auto-generated method stub
	}

	public void visit(ASTMethod method) {
		method.body().accept(this);
	}

	public void visit(ASTMethods methods) {
		for (ASTMethod method : methods) {
			mv = cw.visitMethod( // current method
					ACC_PUBLIC, method.symbol().name(), method.descriptor(), null, null);

			mv.visitCode();

			method.accept(this);

			mv.visitMaxs(0, 0); // Automatically calculated by ASM
			mv.visitEnd();
		}
	}

	public void visit(ASTObject object) {
		String parentName;

		if (object.parentName() != null)
			parentName = object.parentName();
		else
			parentName = defaultParentName;

		cw.visit(V23, ACC_SUPER | ACC_PUBLIC, object.name(), null, parentName, null);

		object.fields().accept(this);

		constructor(object, parentName); // initializers

		object.methods().accept(this);
	}

	public void visit(ASTStmtBlock stmt) {
		for (ASTStatement statement : stmt)
			statement.accept(this);
	}

	public void visit(ASTStmtExpression stmt) {
		stmt.expression().accept(this);
	}

	public void visit(ASTStmtIfThenElse stmt) {
		Label elseLabel = new Label();
		Label endLabel = new Label();
		ASTExpression condition = stmt.condition();
		ASTStatement thenBranch = stmt.thenBranch();
		ASTStatement elseBranch = stmt.elseBranch();

		// Generate bytecode for condition
		condition.accept(this);

		// If condition is false, jump to else (or end if no else)
		mv.visitJumpInsn(IFEQ, elseBranch != null ? elseLabel : endLabel);

		// Generate bytecode for then-branch
		thenBranch.accept(this);

		// Skip else-branch (if it exists)
		if (elseBranch != null) {
			mv.visitJumpInsn(GOTO, endLabel);

			mv.visitLabel(elseLabel);

			elseBranch.accept(this);
		}

		// End label
		mv.visitLabel(endLabel);
	}

	public void visit(ASTStmtReturn stmt) {
		ASTExpression returnValue = stmt.returnValue();

		if (returnValue == null) {
			mv.visitInsn(Opcodes.RETURN);

			return;
		}

		returnValue.accept(this);

		switch (returnValue.lpcType()) {
		case LPCINT:
			mv.visitInsn(Opcodes.IRETURN);
			break;
		case LPCMIXED:
		case LPCSTRING:
		case LPCOBJECT:
			mv.visitInsn(Opcodes.ARETURN);
			break;
		default:
			throw new UnsupportedOperationException("Unsupported return value type: " + returnValue.lpcType());
		}
	}

	private void constructor(ASTObject object, String parentName) {
		mv = cw.visitMethod( // current method
				ACC_PUBLIC, "<init>", "()V", null, null);

		mv.visitCode();

		// Call super constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, parentName, "<init>", "()V", false);

		// Initialize fields
		for (ASTField field : object.fields())
			if (field.initializer() != null) {
				mv.visitVarInsn(ALOAD, 0); // Load 'this'

				field.initializer().accept(this);

				mv.visitFieldInsn(PUTFIELD, object.name(), field.symbol().name(), field.descriptor());
			}

		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0); // Automatically calculated by ASM
		mv.visitEnd();
	}

//    public byte[] bytes() {
//	return cw.toByteArray();
//    }

	private void invokeParamTypes(ASTArguments args) {
		pushInt(args.size());

		mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

		for (int i = 0; i < args.size(); i++) {
			mv.visitInsn(DUP); // Duplicate array reference.

			pushInt(i); // Push array index.

			// Get the LPC type for the i-th argument.
			ASTExpression expr = args.nodes().get(i).expression();
			JType jType = expr.lpcType().jType();

			switch (jType) {
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

			mv.visitInsn(AASTORE);
		}
	}

	private void invokeLoadLocalObj(int slot) {
		mv.visitVarInsn(ALOAD, slot);
	}

	private void invokeGetClass() {
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
	}

	private void invokeGetMethod() {
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
				"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
	}

	private void invokeMethodInvoke() {
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
				"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
	}

	private void invokeReturnValue(LPCType lpcType) {
		if (lpcType != null)
			switch (lpcType.jType()) {
			case JINT:
				// Cast to Integer and unbox to int.
				mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
				break;
			case JFLOAT:
				// Cast to Float and unbox to float.
				mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
				break;
			case JBOOLEAN:
				// Cast to Boolean and unbox to boolean.
				mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
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

	private void pushInt(int value) {
		if ((value >= -1) && (value <= 5))
			mv.visitInsn(Opcodes.ICONST_0 + value);
		else if ((value >= Byte.MIN_VALUE) && (value <= Byte.MAX_VALUE))
			mv.visitIntInsn(Opcodes.BIPUSH, value);
		else if ((value >= Short.MIN_VALUE) && (value <= Short.MAX_VALUE))
			mv.visitIntInsn(Opcodes.SIPUSH, value);
		else
			mv.visitLdcInsn(value);
	}

	public void visit(ASTParameter param) {
		// TODO Auto-generated method stub
	}

	public void visit(ASTParameters params) {
		// TODO Auto-generated method stub
	}
}
