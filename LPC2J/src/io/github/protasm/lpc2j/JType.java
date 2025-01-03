package io.github.protasm.lpc2j;

import java.util.HashMap;
import java.util.Map;

public enum JType {
    JBOOLEAN("Z"), JCHAR("C"), JFLOAT("F"), JDOUBLE("D"), JBYTE("B"), JSHORT("S"), JINT("I"), JLONG("J"),
    JSTRING("Ljava/lang/String;"), JOBJECT("Ljava/lang/Object;"), JVOID("V");

    private final String descriptor;
    private static final Map<String, JType> jTypeForLPCType = new HashMap<>();

    static {
	jTypeForLPCType.put("int", JType.JINT);
	jTypeForLPCType.put("float", JType.JFLOAT);
	jTypeForLPCType.put("status", JType.JBOOLEAN);
	jTypeForLPCType.put("string", JType.JSTRING);
	jTypeForLPCType.put("void", JType.JVOID);
    }

    JType(String descriptor) {
	this.descriptor = descriptor;
    }

    public String descriptor() {
	return descriptor;
    }

    public static JType jTypeForLPCType(String lpcType) {
	return jTypeForLPCType.get(lpcType);
    }

    public static String jDescForLPCType(String lpcType) {
	return jTypeForLPCType.get(lpcType).descriptor();
    }
}