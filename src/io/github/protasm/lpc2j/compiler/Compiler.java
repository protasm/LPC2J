package io.github.protasm.lpc2j.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;

import static org.objectweb.asm.Opcodes.*;

public class Compiler {
	private final String defaultParentName;

	private ClassWriter classWriter;
	private ASTObject astObject;

	public Compiler(String defaultParentName) {
		this.defaultParentName = defaultParentName;
	}

	public byte[] compile(ASTObject astObject) {
		if (astObject == null)
			return null;

		this.astObject = astObject;

		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

		classHeader();
		fields();
		constructor();
		methods();

		return classWriter.toByteArray();
	}

	private void classHeader() {
		classWriter.visit(V23, ACC_SUPER | ACC_PUBLIC, astObject.name(), null, parentName(), null);
	}

	private String parentName() {
		String parentName = astObject.parentName();

		return parentName != null ? parentName : defaultParentName;
	}

	private void fields() {
		for (ASTField field : astObject.fields()) {
			FieldVisitor fv = classWriter.visitField(ACC_PRIVATE, field.symbol().name(), field.descriptor(), null,
					null);

			if (fv != null)
				fv.visitEnd();
		}
	}

	private void constructor() {
		MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);

		mv.visitCode();

		// Call super constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, parentName(), "<init>", "()V", false);

		// Initialize fields
		for (ASTField field : astObject.fields())
			if (field.initializer() != null) {
				mv.visitVarInsn(ALOAD, 0); // Load 'this'

				field.initializer().accept(mv); // Generate bytecode for the initializer

				mv.visitFieldInsn(PUTFIELD, astObject.name(), field.symbol().name(), field.descriptor());
			}

		mv.visitInsn(RETURN);

		mv.visitMaxs(0, 0); // Automatically calculated by ASM
		mv.visitEnd();
	}

	private void methods() {
		for (ASTMethod method : astObject.methods()) {
			MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, method.symbol().name(), method.descriptor(), null,
					null);

			mv.visitCode();

			method.accept(mv);

			mv.visitMaxs(0, 0); // Automatically calculated by ASM
			mv.visitEnd();
		}
	}
}
