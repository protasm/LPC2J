package io.github.protasm.lpc2j;

import static io.github.protasm.lpc2j.BinaryOpType.BOP_ADD;
import static io.github.protasm.lpc2j.InstrType.IT_POP;
import static io.github.protasm.lpc2j.JType.JDOUBLE;
import static io.github.protasm.lpc2j.JType.JFLOAT;
import static io.github.protasm.lpc2j.JType.JINT;
import static io.github.protasm.lpc2j.JType.JLONG;
import static io.github.protasm.lpc2j.JType.JOBJECT;
import static io.github.protasm.lpc2j.JType.JSTRING;
import static io.github.protasm.lpc2j.SymbolType.*;
import static org.objectweb.asm.Opcodes.*;

import java.util.ListIterator;
import java.util.Stack;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Method implements HasSymbol {
    private Symbol symbol;
    private MethodVisitor mv;
    private Stack<Local> locals;
    private Stack<JType> operandTypes;
    private int workingScopeDepth;

    public Method(Symbol symbol, MethodVisitor mv) {
	this.symbol = symbol;
	this.mv = mv;

	locals = new Stack<>();
	operandTypes = new Stack<>();

	// Locals slot 0 reserved for "this" (non-static methods only)
	Symbol localSymbol = new Symbol(symbol.cb(), SYM_LOCAL, JOBJECT, "this", JOBJECT.descriptor());
	Local local = new Local(localSymbol);

	addLocal(local, true);

	workingScopeDepth = 1;

	mv.visitCode();

	if (symbol.identifier().equals("<init>")) {
	    locLoadInstr(0);
	    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
	}
    }

    public Stack<Local> locals() {
	return locals;
    }

    public Stack<JType> operandTypes() {
	return operandTypes;
    }

    public int workingScopeDepth() {
	return workingScopeDepth;
    }

    public void incScopeDepth() {
	workingScopeDepth += 1;
    }

    public void decScopeDepth() {
	workingScopeDepth -= 1;
    }

    public boolean hasLocal(String name) {
	ListIterator<Local> localsItr = locals.listIterator(locals.size());

	while (localsItr.hasPrevious()) {
	    Local local = localsItr.previous();

	    if (local.identifier().equals(name))
		return true;
	}

	return false;
    }

    public int addLocal(Local local, boolean markInitialized) {
	locals.push(local);

	if (markInitialized) {
	    markTopLocalInitialized();
	}

	return locals.size() - 1;
    }

    public void popLocal() {
	locals().pop();

	emitInstr(IT_POP);
    }

    public void markTopLocalInitialized() {
	locals.peek().setScopeDepth(workingScopeDepth);
    }

    public void emitInstr(InstrType instrType, Object... args) {
	switch (instrType) {
	case IT_BINARY:
	    binaryOp((BinaryOpType) args[0]);
	    break;
	case IT_CONST_FLOAT:
	    constFloatInstr((Float) args[0]);
	    break;
	case IT_CONST_INT:
	    constIntInstr((Integer) args[0]);
	    break;
	case IT_CONST_STR:
	    constStrInstr((String) args[0]);
	    break;
	case IT_FIELD_LOAD:
	    fieldLoadInstr((Field) args[0]);
	    break;
	case IT_FIELD_STORE:
	    fieldStoreInstr((Field) args[0]);
	    break;
	case IT_I2F:
	    i2fInstr();
	    break;
	case IT_INVOKE:
	    String invokeOwner = (String) args[0];
	    String invokeName = (String) args[1];
	    String invokeDesc = (String) args[2];

	    invoke(invokeOwner, invokeName, invokeDesc);
	    break;
	case IT_INVOKE_OTHER:
	    Integer invokeOtherLocalIdx = (Integer) args[0];
	    String invokeOtherName = (String) args[1];
	    
	    invokeOther(invokeOtherLocalIdx, invokeOtherName);
	    break;
	case IT_LITERAL:
	    LiteralType lType = (LiteralType) args[0];

	    literalInstr(lType);
	    break;
	case IT_LOC_LOAD:
	    locLoadInstr((Integer) args[0]);
	    break;
	case IT_LOC_STORE:
	    locStoreInstr((Integer) args[0]);
	    break;
	case IT_NEGATE:
	    negateInstr();
	    break;
	case IT_POP:
	    popInstr();
	    break;
	case IT_RETURN:
	    returnInstr();
	    break;
	case IT_RETURNVAL:
	    returnValInstr();
	    break;
	case IT_LOAD_THIS:
	    loadThisInstr();
	    break;
	} // switch (instrType)
    }

    private void binaryOpInts(BinaryOpType op, JType lhsType, JType rhsType) {
	switch (op) {
	case BOP_ADD:
	    mv.visitInsn(IADD);
	    break;
	case BOP_SUB:
	    mv.visitInsn(ISUB);
	    break;
	case BOP_DIV:
	    mv.visitInsn(IDIV);
	    break;
	case BOP_MULT:
	    mv.visitInsn(IMUL);
	    break;
	default:
	    throw new UnsupportedOperationException("Invalid operation for " + lhsType + " and " + rhsType + ".");
	}
    }

    private void binaryOpFloats(BinaryOpType op, JType lhsType, JType rhsType) {
	switch (op) {
	case BOP_ADD:
	    mv.visitInsn(FADD);
	    break;
	case BOP_SUB:
	    mv.visitInsn(FSUB);
	    break;
	case BOP_DIV:
	    mv.visitInsn(FDIV);
	    break;
	case BOP_MULT:
	    mv.visitInsn(FMUL);
	    break;
	default:
	    throw new UnsupportedOperationException("Invalid operation for " + lhsType + " and " + rhsType + ".");
	}
    }

    private void binaryOpStrings() {
	mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
	mv.visitInsn(DUP);
	mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
		"(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    }

    private void binaryOp(BinaryOpType op) {
	JType rhsType = operandTypes.pop();
	JType lhsType = operandTypes.peek();

	// leave LHS Type stacked; reused

	switch (lhsType) {
	case JINT:
	    if (rhsType == JINT)
		binaryOpInts(op, lhsType, rhsType);
	    else if (rhsType == JSTRING && op == BOP_ADD)
		binaryOpStrings();
	    else
		invalidBinaryOp(op, lhsType, rhsType);

	    break;
	case JFLOAT:
	    if (rhsType == JFLOAT)
		binaryOpFloats(op, lhsType, rhsType);
	    else if (rhsType == JSTRING && op == BOP_ADD)
		binaryOpStrings();
	    else
		invalidBinaryOp(op, lhsType, rhsType);

	    break;
	case JSTRING:
	    if ((rhsType == JINT || rhsType == JFLOAT || rhsType == JSTRING) && (op == BOP_ADD))
		binaryOpStrings();
	    else
		invalidBinaryOp(op, lhsType, rhsType);

	    break;
	default:
	    break;
	}
    }

    private void invalidBinaryOp(BinaryOpType op, JType lhsType, JType rhsType) {
	throw new UnsupportedOperationException(
		"Invalid binary operation: " + lhsType + " " + op + " " + rhsType + ".");
    }

    private void constFloatInstr(Float value) {
	operandTypes.push(JFLOAT);

	if (value == 0.0f)
	    mv.visitInsn(FCONST_0);
	else if (value == 1.0f)
	    mv.visitInsn(FCONST_1);
	else if (value == 2.0f)
	    mv.visitInsn(FCONST_2);
	else
	    mv.visitLdcInsn(value);
    }

    private void constIntInstr(Integer value) {
	operandTypes.push(JINT);

	if (value == -1)
	    mv.visitInsn(ICONST_M1);
	else if (value == 0)
	    mv.visitInsn(ICONST_0);
	else if (value == 1)
	    mv.visitInsn(ICONST_1);
	else if (value == 2)
	    mv.visitInsn(ICONST_2);
	else if (value == 3)
	    mv.visitInsn(ICONST_3);
	else if (value == 4)
	    mv.visitInsn(ICONST_4);
	else if (value == 5)
	    mv.visitInsn(ICONST_5);
	else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
	    mv.visitIntInsn(BIPUSH, value);
	else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
	    mv.visitIntInsn(SIPUSH, value);
	else
	    mv.visitLdcInsn(value);
    }

    private void constStrInstr(String value) {
	operandTypes.push(JSTRING);

	mv.visitLdcInsn(value);
    }

    private void fieldLoadInstr(Field field) {
	operandTypes.push(field.jType());

	mv.visitFieldInsn(GETFIELD, field.className(), field.identifier(), field.descriptor());
    }

    private void fieldStoreInstr(Field field) {
	operandTypes.pop(); // value being stored
	operandTypes.pop(); // object reference

	mv.visitFieldInsn(PUTFIELD, field.className(), field.identifier(), field.descriptor());
    }

    private void i2fInstr() {
	operandTypes.pop();
	operandTypes.push(JFLOAT);

	mv.visitInsn(I2F);
    }

    private void invoke(String owner, String name, String desc) {
	mv.visitMethodInsn(INVOKEVIRTUAL, owner, name, desc, false);
    }
    
    private void invokeOther(Integer localIdx, String name) {
	// Step 1: Load local variable representing the target object
	mv.visitVarInsn(ALOAD, localIdx);

	// Step 2: Get the runtime class of target object
	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);

	// Step 3: Push the method name
	mv.visitLdcInsn(name);

	// Step 4: Load the parameter type array for method name
	mv.visitInsn(ICONST_1); // Array size: 1 (for a single int argument)
	mv.visitTypeInsn(ANEWARRAY, "java/lang/Class"); // Create a new array of Class objects
	mv.visitInsn(DUP); // Duplicate the array reference
	mv.visitInsn(ICONST_0); // Index 0
	mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;"); // Load int.class
	mv.visitInsn(AASTORE); // Store int.class in the array

	// Step 5: Use reflection to get the 'bar' method
	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", 
	    "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);

	// Step 6: Load target of the method invocation again
	mv.visitVarInsn(ALOAD, localIdx);

	// Step 7: Prepare the arguments for the method call
	mv.visitInsn(ICONST_1); // Array size: 1
	mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // Create a new Object array
	mv.visitInsn(DUP); // Duplicate the array reference
	mv.visitInsn(ICONST_0); // Index 0
	mv.visitLdcInsn(9); // Push the integer value 9
	mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false); // Box int to Integer
	mv.visitInsn(AASTORE); // Store the boxed Integer in the array

	// Step 8: Dynamically invoke the method
	mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", 
	    "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);

	// Step 9: Handle the return value (if any)
	mv.visitInsn(POP); // Discard the return value since 'bar' returns void
    }

    private void literalInstr(LiteralType lType) {
	switch (lType) {
	case LT_TRUE:
	    mv.visitInsn(ICONST_1);
	    break;
	case LT_FALSE:
	    mv.visitInsn(ICONST_0);
	    break;
	case LT_NULL:
	    mv.visitInsn(ACONST_NULL);
	    break;
	}
    }

    private void loadThisInstr() {
	mv.visitVarInsn(ALOAD, 0);
    }

    private void locLoadInstr(int idx) {
	Local local = locals.get(idx);
	JType type = local.jType();

	switch (type) {
	case JINT:
	    mv.visitVarInsn(ILOAD, idx);
	    break;
	case JFLOAT:
	    mv.visitVarInsn(FLOAD, idx);
	    break;
	case JOBJECT:
	case JSTRING:
	    mv.visitVarInsn(ALOAD, idx);
	    break;
	default:
	    return;
	} // switch (type)

	operandTypes.push(type);
    }

    private void locStoreInstr(int idx) {
	Local local = locals.get(idx);
	JType type = local.jType();

	switch (type) {
	case JINT:
	    mv.visitVarInsn(ISTORE, idx);
	    break;
	case JFLOAT:
	    mv.visitVarInsn(FSTORE, idx);
	    break;
	case JOBJECT:
	case JSTRING:
	    mv.visitVarInsn(ASTORE, idx);
	    break;
	default:
	    return;
	} // switch(type)

	operandTypes.pop();
    }

    private void negateInstr() {
	JType type = operandTypes.peek();

	if (type == JINT) {
	    mv.visitInsn(INEG);
	} else if (type == JFLOAT) {
	    mv.visitInsn(FNEG);
	} else if (type == JLONG) {
	    mv.visitInsn(LNEG);
	} else if (type == JDOUBLE) {
	    mv.visitInsn(DNEG);
	} else {
	    throw new IllegalArgumentException("Unsupported type for negation: " + type);
	}
    }

    private void popInstr() {
	operandTypes.pop();

	mv.visitInsn(Opcodes.POP);
    }

    private void returnInstr() {
	mv.visitInsn(RETURN);
    }

    private void returnValInstr() {
	JType type = operandTypes.peek();

	if (type == JINT) {
	    mv.visitInsn(IRETURN);
	} else if (type == JFLOAT) {
	    mv.visitInsn(FRETURN);
	} else if (type == JSTRING) {
	    mv.visitInsn(ARETURN);
	}
    }

    public void finish() {
	mv.visitMaxs(0, 0);

	mv.visitEnd();
    }

    @Override
    public String className() {
	return symbol.className();
    }

    @Override
    public SymbolType sType() {
	return symbol.sType();
    }

    @Override
    public JType jType() {
	return symbol.jType();
    }

    @Override
    public String identifier() {
	return symbol.identifier();
    }

    @Override
    public String descriptor() {
	return symbol.descriptor();
    }
}