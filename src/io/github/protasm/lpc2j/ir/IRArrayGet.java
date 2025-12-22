package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public final class IRArrayGet implements IRExpression {
    private final int line;
    private final IRExpression array;
    private final IRExpression index;
    private final RuntimeType type;

    public IRArrayGet(int line, IRExpression array, IRExpression index, RuntimeType type) {
        this.line = line;
        this.array = Objects.requireNonNull(array, "array");
        this.index = Objects.requireNonNull(index, "index");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public IRExpression array() {
        return array;
    }

    public IRExpression index() {
        return index;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
