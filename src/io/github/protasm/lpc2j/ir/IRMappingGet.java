package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public final class IRMappingGet implements IRExpression {
    private final int line;
    private final IRExpression mapping;
    private final IRExpression key;
    private final RuntimeType type;

    public IRMappingGet(int line, IRExpression mapping, IRExpression key, RuntimeType type) {
        this.line = line;
        this.mapping = Objects.requireNonNull(mapping, "mapping");
        this.key = Objects.requireNonNull(key, "key");
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public IRExpression mapping() {
        return mapping;
    }

    public IRExpression key() {
        return key;
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
