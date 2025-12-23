package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

public record IRInstanceCall(
        int line,
        String ownerInternalName,
        String methodName,
        boolean parentDispatch,
        List<IRExpression> arguments,
        List<RuntimeType> parameterTypes,
        RuntimeType type) implements IRExpression {
    public IRInstanceCall {
        Objects.requireNonNull(ownerInternalName, "ownerInternalName");
        Objects.requireNonNull(methodName, "methodName");
        Objects.requireNonNull(parameterTypes, "parameterTypes");
        arguments = List.copyOf(arguments);
        parameterTypes = List.copyOf(parameterTypes);
        Objects.requireNonNull(type, "type");
    }
}
