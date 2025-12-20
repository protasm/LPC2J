package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;

public record IRDynamicInvoke(
        int line, IRLocal targetLocal, String methodName, List<IRExpression> arguments, RuntimeType type)
        implements IRExpression {
    public IRDynamicInvoke {
        Objects.requireNonNull(targetLocal, "targetLocal");
        Objects.requireNonNull(methodName, "methodName");
        arguments = List.copyOf(arguments);
        Objects.requireNonNull(type, "type");
    }
}
