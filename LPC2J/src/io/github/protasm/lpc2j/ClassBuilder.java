package io.github.protasm.lpc2j;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ClassBuilder {
    private String name;
    private ClassWriter cw;
    private MethodBuilder mb;
    private Map<String, Variable> fields = new HashMap<>();

    public ClassBuilder(String className) {
	this.name = className;

	cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

	cw.visit(Opcodes.V23, Opcodes.ACC_SUPER, className, null, "java/lang/Object", null);
    }

    public void field(String name, String desc) {
	cw.visitField(0, name, desc, null, null).visitEnd();
    }

    public void constructor() {
	method(J_Type.VOID, "<init>", "()V");
    }

    public void method(J_Type returnType, String name, String desc) {
	mb = new MethodBuilder(this, returnType, name, desc);
    }

    public ClassWriter cw() {
	return cw;
    }

    public MethodBuilder mb() {
	return mb;
    }

    public String className() {
	return name;
    }

    public void addField(Variable jVar) {
	fields.put(jVar.name(), jVar);
    }

    public Variable getField(String name) {
	return fields.get(name);
    }

    public boolean hasField(String name) {
	return fields.containsKey(name);
    }

    public byte[] bytes() {
	return cw.toByteArray();
    }

    public void finish() {
	cw.visitEnd();
    }
}
