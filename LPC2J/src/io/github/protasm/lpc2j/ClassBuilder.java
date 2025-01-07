package io.github.protasm.lpc2j;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.scanner.Token;

public class ClassBuilder {
    private String name;
    private ClassWriter cw;
    private Method currMethod;

    private Map<String, Field> fields = new HashMap<>();
    private Map<String, Method> methods = new HashMap<>();

    public ClassBuilder(String name) {
	this.name = "brainjar/" + name;

	cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

	cw.visit(Opcodes.V23, Opcodes.ACC_SUPER, this.name, null, "io/github/protasm/lpc2j/LPCObject", null);
    }

    public String name() {
	return name;
    }

    public ClassWriter cw() {
	return cw;
    }

    public Method mb() {
	return currMethod;
    }

    public void field(Field field) {
	cw.visitField(0, field.name(), field.desc(), null, null).visitEnd();
	fields.put(field.name(), field);
    }

    public void newMethod(JType jType, String name, String fullDesc) {
	currMethod = new Method(this, jType, name, fullDesc);
	methods.put(currMethod.name(), currMethod);
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
