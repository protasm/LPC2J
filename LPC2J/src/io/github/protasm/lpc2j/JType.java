package io.github.protasm.lpc2j;

public enum JType {
    JBOOLEAN("Z"), JCHAR("C"), JFLOAT("F"), JDOUBLE("D"), JBYTE("B"), JSHORT("S"), JINT("I"), JLONG("J"),
    JSTRING("Ljava/lang/String;"), JOBJECT("Lio/github/protasm/lpc2j/LPCObject;"), JVOID("V");

    private final String descriptor;

    JType(String descriptor) {
	this.descriptor = descriptor;
    }

    public String descriptor() {
	return descriptor;
    }
}