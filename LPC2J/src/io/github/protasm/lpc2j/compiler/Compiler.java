package io.github.protasm.lpc2j.compiler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.scanner.TokenList;

public class Compiler {
    private ClassWriter classWriter;
    private ASTObject astObject;

    public byte[] compile(ASTObject astObject) {
	this.astObject = astObject;

	classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

	classHeader();
	fields();
	constructor();
	methods();

	return classWriter.toByteArray();
    }

    private void classHeader() {
	String className = astObject.name();

	classWriter.visit(Opcodes.V23, Opcodes.ACC_SUPER, className, null, superClassName(), null);
    }

    private String superClassName() {
	return astObject.parentName() != null ? astObject.parentName() : "java/lang/Object";
    }

    private void fields() {
	for (ASTField field : astObject.fields()) {
	    FieldVisitor fv = classWriter.visitField(Opcodes.ACC_PRIVATE, field.name(), field.descriptor(), null, null);

	    if (fv != null)
		fv.visitEnd();
	}
    }

    private void constructor() {
	MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);

	mv.visitCode();

	// Call super constructor
	mv.visitVarInsn(Opcodes.ALOAD, 0);
	mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

	// Initialize fields
	for (ASTField field : astObject.fields())
	    if (field.initializer() != null) {
		mv.visitVarInsn(Opcodes.ALOAD, 0); // Load 'this'
		
		field.initializer().toBytecode(mv); // Generate bytecode for the initializer
		
		mv.visitFieldInsn(Opcodes.PUTFIELD, astObject.name(), field.name(), field.descriptor());
	    }

	mv.visitInsn(Opcodes.RETURN);
	
	mv.visitMaxs(0, 0); // Automatically calculated by ASM
	mv.visitEnd();
    }

    private void methods() {
	for (ASTMethod method : astObject.methods()) {
	    MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, method.name(), method.descriptor(), null, null);

	    mv.visitCode();

	    method.toBytecode(mv);

	    mv.visitMaxs(0, 0); // Automatically calculated by ASM
	    mv.visitEnd();
	}
    }

    private void writeToFile(String directory, String prefix, byte[] bytecode) {
	try {
	    Path outputPath = Paths.get(directory, prefix + ".class");

	    Files.write(outputPath, bytecode);

	    System.out.println(prefix + ".class created in " + directory + ".");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	if (args.length != 1) {
	    System.err.println("Usage: java Compiler <source-file>");
	    System.exit(1);
	}

	Path filePath = Paths.get(args[0]);
	String directory = filePath.getParent().toString();
	String fileName = filePath.getFileName().toString();
	String prefix = fileName.substring(0, fileName.indexOf('.'));

	try {
	    String source = Files.readString(filePath);

	    Scanner scanner = new Scanner();
	    TokenList tokens = scanner.scan(source);

	    Parser parser = new Parser();
	    ASTObject ast = parser.parse(prefix, tokens);

	    Compiler compiler = new Compiler();
	    byte[] bytes = compiler.compile(ast);

	    compiler.writeToFile(directory, prefix, bytes);

	    System.out.println("Compilation successful.");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}
