package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public record IRFieldLoad(int line, IRField field) implements IRExpression {
    public IRFieldLoad {
        Objects.requireNonNull(field, "field");
    }

    @Override
    public RuntimeType type() {
        return field.type();
    }
}
