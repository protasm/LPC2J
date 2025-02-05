package io.github.protasm.lpc2j;

//import java.util.HashMap;
//import java.util.Map;

import static io.github.protasm.lpc2j.JType.*;

public enum LPCType {
	LPCINT(JINT), LPCFLOAT(JFLOAT), LPCMAPPING(null), LPCMIXED(null), LPCNULL(JNULL), LPCOBJECT(JLPCOBJECT),
	LPCSTATUS(JBOOLEAN), LPCSTRING(JSTRING), LPCVOID(JVOID);

	private final JType jType;

	LPCType(JType jType) {
		this.jType = jType;
	}

	public JType jType() {
		return jType;
	}
}
