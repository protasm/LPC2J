package io.github.protasm.lpc2j;

public enum JType {
	JBOOLEAN("Z"), JCHAR("C"), JFLOAT("F"), JDOUBLE("D"), JBYTE("B"), JSHORT("S"), JINT("I"), JLONG("J"), JNULL(null),
	JSTRING("Ljava/lang/String;"), JLPCOBJECT("Lio/github/protasm/lpc2j/runtime/LPCObject;"), JVOID("V");

	private final String descriptor;

	JType(String descriptor) {
		this.descriptor = descriptor;
	}

	public String descriptor() {
		return descriptor;
	}
}