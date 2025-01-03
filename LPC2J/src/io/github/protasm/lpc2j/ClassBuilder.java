package io.github.protasm.lpc2j;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ClassBuilder {
    private String name;
    private ClassWriter cw;
    private MethodBuilder mb;
    private Map<String, Field> fields = new HashMap<>();

    public ClassBuilder(String className) {
	this.name = className;

	cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

	cw.visit(Opcodes.V23, Opcodes.ACC_SUPER, className, null, "java/lang/Object", null);
    }

    public void field(Field field) {
	cw.visitField(0, field.name(), field.desc(), null, null).visitEnd();
	fields.put(field.name(), field);
    }

    public void constructor() {
	method(JType.JVOID, "<init>", "()V");
    }

    public void method(JType returnType, String name, String desc) {
	mb = new MethodBuilder(this, returnType, name, desc);
    }

    public String name() {
	return name;
    }

    public ClassWriter cw() {
	return cw;
    }

    public MethodBuilder mb() {
	return mb;
    }

    public Field getField(String name) {
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
