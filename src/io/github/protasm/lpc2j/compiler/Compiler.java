package io.github.protasm.lpc2j.compiler;

import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.fs.FSFile;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.scanner.Tokens;

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
	for (ASTField field : astObject.fields().values()) {
	    FieldVisitor fv = classWriter.visitField(ACC_PRIVATE, field.name(), field.descriptor(), null, null);

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
	for (ASTField field : astObject.fields().values())
	    if (field.initializer() != null) {
		mv.visitVarInsn(ALOAD, 0); // Load 'this'

		field.initializer().toBytecode(mv); // Generate bytecode for the initializer

		mv.visitFieldInsn(PUTFIELD, astObject.name(), field.name(), field.descriptor());
	    }

	mv.visitInsn(RETURN);

	mv.visitMaxs(0, 0); // Automatically calculated by ASM
	mv.visitEnd();
    }

    private void methods() {
	for (ASTMethod method : astObject.methods().values()) {
	    MethodVisitor mv = classWriter.visitMethod(ACC_PUBLIC, method.name(), method.descriptor(), null, null);

	    mv.visitCode();

	    method.toBytecode(mv);

	    mv.visitMaxs(0, 0); // Automatically calculated by ASM
	    mv.visitEnd();
	}
    }

    public static void main(String[] args) throws IOException {
	if (args.length != 1) {
	    System.err.println("Usage: java Compiler <source-file>");

	    System.exit(1);
	}

	FSFile sf = new FSFile("/Users/jonathan/brainjar/", args[0]);
	Scanner scanner = new Scanner();
	Tokens tokens = scanner.scan(sf.source());
	Parser parser = new Parser();
	ASTObject ast = parser.parse(sf.slashName(), tokens);
	Compiler compiler = new Compiler("io/github/protasm/lpc2j/runtime/LPCObject");

	byte[] bytes = compiler.compile(ast);

	sf.write(bytes);

	System.out.println("Compilation successful.");
    }
}
