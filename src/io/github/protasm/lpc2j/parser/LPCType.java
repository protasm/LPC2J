package io.github.protasm.lpc2j.parser;

import static io.github.protasm.lpc2j.parser.JType.*;

public enum LPCType {
    LPCINT(JINT), LPCFLOAT(JFLOAT), LPCMAPPING(null), LPCMIXED(JOBJECT), LPCNULL(JNULL), LPCOBJECT(JOBJECT),
    LPCSTATUS(JBOOLEAN), LPCSTRING(JSTRING), LPCVOID(JVOID);

    private final JType jType;

    LPCType(JType jType) {
	this.jType = jType;
    }

    public JType jType() {
	return jType;
    }
}
