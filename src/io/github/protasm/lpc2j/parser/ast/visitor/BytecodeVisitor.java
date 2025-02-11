package io.github.protasm.lpc2j.parser.ast.visitor;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V23;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCall;
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
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class BytecodeVisitor {
    private String defaultParentName;
    private ClassWriter cw;
    private ASTObject object; // current object

    public BytecodeVisitor(String defaultParentName) {
	this.defaultParentName = defaultParentName;

	cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    }

    private void constructor(ASTObject object, String parentName) {
	MethodVisitor mv = cw.visitMethod(
		ACC_PUBLIC,
		"<init>",
		"()V",
		null, null);

	mv.visitCode();

	// Call super constructor
	mv.visitVarInsn(ALOAD, 0);
	mv.visitMethodInsn(
		INVOKESPECIAL,
		parentName,
		"<init>",
		"()V",
		false);

	// Initialize fields
	for (ASTField field : object.fields())
	    if (field.initializer() != null) {
		mv.visitVarInsn(ALOAD, 0); // Load 'this'

		field.initializer().accept(this);

		mv.visitFieldInsn(
			PUTFIELD,
			object.name(),
			field.symbol().name(),
			field.descriptor());
	    }

	mv.visitInsn(RETURN);

	mv.visitMaxs(0, 0); // Automatically calculated by ASM
	mv.visitEnd();
    }

    public byte[] bytes() {
	return cw.toByteArray();
    }

    public void visit(ASTArgument argument, MethodVisitor mv) {
	argument.expression().accept(this);

	LPCType type = argument.expression().lpcType();

	if (type == null) // Without type information, assume no boxing is needed.
	    return;

	// box primitive value, if needed
	switch (type.jType()) {
	case JINT: // Integer.valueOf(int)
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf",
		    "(I)Ljava/lang/Integer;", false);
	break;
	case JFLOAT: // Float.valueOf(float)
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf",
		    "(F)Ljava/lang/Float;", false);
	break;
	case JBOOLEAN: // Boolean.valueOf(boolean)
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf",
		    "(Z)Ljava/lang/Boolean;", false);
	break;
	default: // For non-primitive types (or types that don't need boxing).
	break;
	}
    }

    public void visit(ASTArguments arguments) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprCall expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprFieldAccess expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprFieldStore expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprInvokeLocal expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprLiteralFalse expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprLiteralInteger expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprLiteralString expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprLiteralTrue expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprLocalAccess expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprLocalStore expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprNull expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprOpBinary expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTExprOpUnary expr) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTField field) {
	FieldVisitor fv = cw.visitField(
		ACC_PRIVATE,
		field.symbol().name(),
		field.descriptor(),
		null,
		null);

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
	// TODO Auto-generated method stub

    }

    public void visit(ASTMethods methods) {
	for (ASTMethod method : methods) {
	    MethodVisitor mv = cw.visitMethod(
		    ACC_PUBLIC,
		    method.symbol().name(),
		    method.descriptor(),
		    null, null);

	    mv.visitCode();

	    method.accept(mv);

	    mv.visitMaxs(0, 0); // Automatically calculated by ASM
	    mv.visitEnd();
	}
    }

    public void visit(ASTObject object) {
	this.object = object;

	String parentName;

	if (object.parentName() != null)
	    parentName = object.parentName();
	else
	    parentName = defaultParentName;

	cw.visit(
		V23,
		ACC_SUPER | ACC_PUBLIC,
		object.name(),
		null,
		parentName,
		null);

	object.fields().accept(this);

	constructor(object, parentName); // initializers

	object.methods().accept(this);
    }

    public void visit(ASTParameter parameter) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTParameters parameters) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTStmtBlock stmt) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTStmtExpression stmt) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTStmtIfThenElse stmt) {
	// TODO Auto-generated method stub

    }

    public void visit(ASTStmtReturn stmt) {
	// TODO Auto-generated method stub

    }
}
