package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

public record IRDynamicInvokeField(
        int line, IRField targetField, String methodName, List<IRExpression> arguments, RuntimeType type)
        implements IRExpression {
    public IRDynamicInvokeField {
        Objects.requireNonNull(targetField, "targetField");
        Objects.requireNonNull(methodName, "methodName");
        arguments = List.copyOf(arguments);
        Objects.requireNonNull(type, "type");
    }
}
