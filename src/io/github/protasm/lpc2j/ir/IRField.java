package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import io.github.protasm.lpc2j.ir.IRExpression;
import java.util.Objects;

public record IRField(int line, String name, RuntimeType type, IRExpression initializer) {
    public IRField {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }
}
