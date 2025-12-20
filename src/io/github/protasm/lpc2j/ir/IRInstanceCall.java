package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

public record IRInstanceCall(
        int line,
        String ownerInternalName,
        String methodName,
        List<IRExpression> arguments,
        RuntimeType type) implements IRExpression {
    public IRInstanceCall {
        Objects.requireNonNull(ownerInternalName, "ownerInternalName");
        Objects.requireNonNull(methodName, "methodName");
        arguments = List.copyOf(arguments);
        Objects.requireNonNull(type, "type");
    }
}
