package io.github.protasm.lpc2j;

import java.util.HashMap;
import java.util.Map;

public enum J_Type {
	BOOLEAN("Z"), CHAR("C"), FLOAT("F"), DOUBLE("D"), BYTE("B"), SHORT("S"), INT("I"), LONG("J"),
	STRING("Ljava/lang/String;"), OBJECT("Ljava/lang/Object;"), VOID("V");

	private final String descriptor;
	private static final Map<String, J_Type> jTypeForLPCType = new HashMap<>();

	static {
		jTypeForLPCType.put("int", J_Type.INT);
		jTypeForLPCType.put("float", J_Type.FLOAT);
		jTypeForLPCType.put("status", J_Type.BOOLEAN);
		jTypeForLPCType.put("string", J_Type.STRING);
		jTypeForLPCType.put("void", J_Type.VOID);
	}

	J_Type(String descriptor) {
		this.descriptor = descriptor;
	}

	public String descriptor() {
		return descriptor;
	}

	public static J_Type jTypeForLPCType(String lpcType) {
		return jTypeForLPCType.get(lpcType);
	}

	public static String jDescForLPCType(String lpcType) {
		return jTypeForLPCType.get(lpcType).descriptor();
	}
}