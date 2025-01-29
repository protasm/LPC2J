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

//    public JType toJType() {
//	return jTypeForLPCType.get(this);
//    }
//
//    private static final Map<LPCType, JType> jTypeForLPCType = new HashMap<>();
//
//    static {
//	jTypeForLPCType.put(LPCINT, JINT);
//	jTypeForLPCType.put(LPCFLOAT, JFLOAT);
//	jTypeForLPCType.put(LPCOBJECT, JOBJECT);
//	jTypeForLPCType.put(LPCSTATUS, JBOOLEAN);
//	jTypeForLPCType.put(LPCSTRING, JSTRING);
//	jTypeForLPCType.put(LPCVOID, JVOID);
//    }
}
