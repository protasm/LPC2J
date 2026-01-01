package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public record IRConstant(int line, Object value, RuntimeType type) implements IRExpression {
    public IRConstant {
        Objects.requireNonNull(type, "type");
    }
}
