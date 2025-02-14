package io.github.protasm.lpc2j.parser.type;

import static io.github.protasm.lpc2j.parser.type.JType.JBOOLEAN;
import static io.github.protasm.lpc2j.parser.type.JType.JFLOAT;
import static io.github.protasm.lpc2j.parser.type.JType.JINT;
import static io.github.protasm.lpc2j.parser.type.JType.JNULL;
import static io.github.protasm.lpc2j.parser.type.JType.JOBJECT;
import static io.github.protasm.lpc2j.parser.type.JType.JSTRING;
import static io.github.protasm.lpc2j.parser.type.JType.JVOID;

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
