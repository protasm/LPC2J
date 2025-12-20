package io.github.protasm.lpc2j.runtime;

import io.github.protasm.lpc2j.parser.type.JType;
import java.util.Objects;

/**
 * Represents the runtime type carried through IR and code generation. Unlike {@link
 * io.github.protasm.lpc2j.parser.type.LPCType}, this model encodes how values are represented on
 * the JVM (primitive descriptors, reference internal names, and array element shapes).
 */
public record RuntimeType(
        RuntimeValueKind kind,
        JType jvmType,
        String objectInternalName,
        RuntimeTruthiness truthiness,
        RuntimeType elementType) {
    public RuntimeType {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(truthiness, "truthiness");
    }

    public boolean isArray() {
        return kind == RuntimeValueKind.ARRAY;
    }

    public boolean isReferenceLike() {
        return jvmType == null || jvmType == JType.JOBJECT || jvmType == JType.JSTRING || jvmType == JType.JNULL;
    }

    /**
     * Returns the JVM descriptor for this runtime type.
     *
     * <p>For primitive-backed types, this delegates to {@link JType#descriptor()}. For reference
     * types, it favors the provided {@code objectInternalName} when available, otherwise defaulting
     * to {@code java/lang/Object}. Array descriptors are derived from the element type.</p>
     */
    public String descriptor() {
        if (isArray())
            return "[" + elementType.descriptor();

        if (jvmType != null && jvmType != JType.JOBJECT && jvmType != JType.JNULL)
            return jvmType.descriptor();

        if (objectInternalName != null)
            return "L" + objectInternalName + ";";

        if (jvmType != null && jvmType.descriptor() != null)
            return jvmType.descriptor();

        return JType.JOBJECT.descriptor();
    }
}
