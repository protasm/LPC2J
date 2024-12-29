package io.github.protasm.lpc2j;

import java.util.ListIterator;
import java.util.Stack;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodBuilder {
	private ClassBuilder cb;
	private J_Type returnType;
	private String name;
	private String desc;
	private MethodVisitor mv;
	private Stack<Local> locals;
	private Stack<J_Type> operandTypes;
	private int workingScopeDepth;

	public MethodBuilder(ClassBuilder cb, J_Type returnType, String name, String desc) {
		this.cb = cb;
		this.returnType = returnType;
		this.name = name;
		this.desc = desc;

		MethodVisitor mv = cb.cw().visitMethod(0, name, desc, null, null);

		this.mv = mv;

		locals = new Stack<>();
		operandTypes = new Stack<>();

		// Locals slot 0 reserved for "this" (non-static methods only)
		Variable jVar = new Variable(J_Type.OBJECT, "this");
		Local local = new Local(jVar);
		addLocal(local, true);

		workingScopeDepth = 1;

		mv.visitCode();

		if (name.equals("<init>")) {
			locLoadInstr(0);
			methodInstr(Opcodes.INVOKESPECIAL, "java/lang/Object", name, desc);
		}
	}

	public J_Type returnType() {
		return returnType();
	}

	public String name() {
		return name;
	}

	public String desc() {
		return desc;
	}

	public MethodVisitor mv() {
		return mv;
	}

	public Stack<Local> locals() {
		return locals;
	}

	public Stack<J_Type> operandTypes() {
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

			if (local.jVar().name().equals(name)) {
				return true;
			}
		}

		return false;
	}

	public int addLocal(Local local, boolean markInitialized) {
		locals.push(local);

		if (markInitialized)
			markTopLocalInitialized();

		return locals.size() - 1;
	}

	public void markTopLocalInitialized() {
		locals.peek().setScopeDepth(workingScopeDepth);
	}

	public void emitInstr(InstrType instrType, Object... args) {
		switch (instrType) {
		case CONST_FLOAT:
			constFloatInstr((Float) args[0]);
			break;
		case CONST_INT:
			constIntInstr((Integer) args[0]);
			break;
		case CONST_OBJ:
			constObjInstr(args[0]);
			break;
		case CONST_STR:
			constStrInstr((String) args[0]);
			break;
		case FIELD_LOAD:
			fieldLoadInstr((Variable) args[0]);
			break;
		case FIELD_STORE:
			fieldStoreInstr((Variable) args[0]);
			break;
		case LOC_LOAD:
			locLoadInstr((Integer) args[0]);
			break;
		case LOC_STORE:
			locStoreInstr((Integer) args[0]);
			break;
		case METHOD:
			int methodOpcode = (Integer) args[0];
			String owner = (String) args[1];
			String name = (String) args[2];
			String desc = (String) args[3];

			methodInstr(methodOpcode, owner, name, desc);

			break;
		case NEGATE:
			negateInstr();
			break;
		case POP:
			popInstr();
			break;
		case RETURN:
			returnInstr();
			break;
		} // switch (instrType)
	}

	private void constFloatInstr(Float value) {
		operandTypes.push(J_Type.FLOAT);

		if (value == 0.0f)
			mv.visitInsn(Opcodes.FCONST_0);
		else if (value == 1.0f)
			mv.visitInsn(Opcodes.FCONST_1);
		else if (value == 2.0f)
			mv.visitInsn(Opcodes.FCONST_2);
		else
			mv.visitLdcInsn(value);
	}
	
	private void constIntInstr(Integer value) {
		operandTypes.push(J_Type.INT);

		if (value == -1)
			mv.visitInsn(Opcodes.ICONST_M1);
		else if (value == 0)
			mv.visitInsn(Opcodes.ICONST_0);
		else if (value == 1)
			mv.visitInsn(Opcodes.ICONST_1);
		else if (value == 2)
			mv.visitInsn(Opcodes.ICONST_2);
		else if (value == 3)
			mv.visitInsn(Opcodes.ICONST_3);
		else if (value == 4)
			mv.visitInsn(Opcodes.ICONST_4);
		else if (value == 5)
			mv.visitInsn(Opcodes.ICONST_5);
		else
			if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
				mv.visitIntInsn(Opcodes.BIPUSH, value);
			else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
				mv.visitIntInsn(Opcodes.SIPUSH, value);
			else
				mv.visitLdcInsn(value);
	}
	
	private void constObjInstr(Object value) {
//		operandTypes.push(J_Type.OBJECT);
	}
	
	private void constStrInstr(String value) {
		operandTypes.push(J_Type.STRING);
		
		mv.visitLdcInsn(value);
	}
	
	private void fieldLoadInstr(Variable field) {
		operandTypes.pop();
		operandTypes.push(field.type());

		mv.visitFieldInsn(Opcodes.GETFIELD, cb.className(), field.name(), field.desc());
	}

	private void fieldStoreInstr(Variable field) {
		operandTypes.pop(); // value being stored
		operandTypes.pop(); // object reference

		mv.visitFieldInsn(Opcodes.PUTFIELD, cb.className(), field.name(), field.desc());
	}

	private void methodInstr(int opCode, String owner, String name, String desc) {
		// operandTypes management needed here?
		mv.visitMethodInsn(opCode, owner, name, desc, false);
	}

	private void locLoadInstr(int idx) {
		Local local = locals.get(idx);
		J_Type type = local.jVar().type();

		switch (type) {
		case INT:
			mv.visitVarInsn(Opcodes.ILOAD, idx);
			break;
		case FLOAT:
			mv.visitVarInsn(Opcodes.FLOAD, idx);
			break;
		case OBJECT:
		case STRING:
			mv.visitVarInsn(Opcodes.ALOAD, idx);
			break;
		default:
			return;
		} // switch(type)

		operandTypes.push(type);
	}

	private void locStoreInstr(int idx) {
		Local local = locals.get(idx);
		J_Type type = local.jVar().type();

		switch (type) {
		case INT:
			mv.visitVarInsn(Opcodes.ISTORE, idx);
			break;
		case FLOAT:
			mv.visitVarInsn(Opcodes.FSTORE, idx);
			break;
		case OBJECT:
		case STRING:
			mv.visitVarInsn(Opcodes.ASTORE, idx);
			break;
		default:
			return;
		} // switch(type)

		operandTypes.pop();
	}
	
	private void negateInstr() {
		J_Type type = operandTypes.peek();
		
		if (type == J_Type.INT)
			mv.visitInsn(Opcodes.INEG);
		else if (type == J_Type.FLOAT)
			mv.visitInsn(Opcodes.FNEG);
		else if (type == J_Type.LONG)
			mv.visitInsn(Opcodes.LNEG);
		else if (type == J_Type.DOUBLE)
			mv.visitInsn(Opcodes.DNEG);
		else
			throw new IllegalArgumentException("Unsupported type for negation: " + type);
	}

	private void popInstr() {
		operandTypes.pop();

		mv.visitInsn(Opcodes.POP);
	}

	private void returnInstr() {
		mv.visitInsn(Opcodes.RETURN);
	}

	public void finish() {
		mv.visitMaxs(0, 0);

		mv.visitEnd();
	}
}