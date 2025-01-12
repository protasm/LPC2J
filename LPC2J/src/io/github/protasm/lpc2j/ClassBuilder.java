package io.github.protasm.lpc2j;

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.scanner.Token;

public class ClassBuilder {
    private String className;
    private ClassWriter cw;
    private Handle bootstrapMethod;
    private Method currMethod;

    private Map<String, Field> fields = new HashMap<>();
    private Map<String, Method> methods = new HashMap<>();

    public ClassBuilder(String className) {
	this.className = className;

	cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

	// Define the bootstrap method handle
	bootstrapMethod = new Handle(H_INVOKESTATIC, "LPCBootstrap", "bootstrap",
		"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
		false);

	cw.visit(Opcodes.V23, Opcodes.ACC_SUPER, className, null, "java/lang/Object", null);
    }

    public String className() {
	return className;
    }

    public ClassWriter cw() {
	return cw;
    }

    public Method currMethod() {
	return currMethod;
    }

    public void newField(JType jType, String identifier) {
	Symbol symbol = new Symbol(this, SymbolType.SYM_FIELD, jType, identifier, jType.descriptor());
	Field field = new Field(symbol);

	cw.visitField(0, identifier, jType.descriptor(), null, null).visitEnd();
	fields.put(identifier, field);
    }

    public void newMethod(JType jType, String identifier, String descriptor) {
	Symbol symbol = new Symbol(this, SymbolType.SYM_METHOD, jType, identifier, descriptor);
	MethodVisitor mv = cw.visitMethod(0, identifier, descriptor, null, null);
	currMethod = new Method(symbol, mv, bootstrapMethod);

	methods.put(identifier, currMethod);
    }

    public void newMethod(Token typeToken, Token nameToken, String paramsDesc) {
	String lpcType = typeToken.lexeme();
	JType jType = JType.jTypeForLPCType(lpcType);
	String name = nameToken.lexeme();

	this.newMethod(jType, name, paramsDesc + jType.descriptor());
    }

    public void constructor() {
	newMethod(JType.JVOID, "<init>", "()V");
    }

    public boolean hasField(String name) {
	return fields.containsKey(name);
    }

    public Field getField(String name) {
	return fields.get(name);
    }

    public boolean hasMethod(String name) {
	return methods.containsKey(name);
    }

    public Method getMethod(String name) {
	return methods.get(name);
    }

    public byte[] bytes() {
	return cw.toByteArray();
    }

    public void finish() {
	cw.visitEnd();
    }
}
