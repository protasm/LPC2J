package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public final class IRArrayConcat implements IRExpression {
    private final int line;
    private final IRExpression left;
    private final IRExpression right;
    private final RuntimeType type;

    public IRArrayConcat(int line, IRExpression left, IRExpression right, RuntimeType type) {
        this.line = line;
        this.left = Objects.requireNonNull(left, "left");
        this.right = Objects.requireNonNull(right, "right");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public IRExpression left() {
        return left;
    }

    public IRExpression right() {
        return right;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
