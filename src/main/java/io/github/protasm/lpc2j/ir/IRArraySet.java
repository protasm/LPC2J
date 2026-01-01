package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public final class IRArraySet implements IRExpression {
    private final int line;
    private final IRExpression array;
    private final IRExpression index;
    private final IRExpression value;
    private final RuntimeType type;

    public IRArraySet(int line, IRExpression array, IRExpression index, IRExpression value, RuntimeType type) {
        this.line = line;
        this.array = Objects.requireNonNull(array, "array");
        this.index = Objects.requireNonNull(index, "index");
        this.value = Objects.requireNonNull(value, "value");
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

    public IRExpression value() {
        return value;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
