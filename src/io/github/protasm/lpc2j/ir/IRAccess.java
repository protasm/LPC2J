package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public record IRLocalLoad(int line, IRLocal local) implements IRExpression {
    public IRLocalLoad {
        Objects.requireNonNull(local, "local");
    }

    @Override
    public RuntimeType type() {
        return local.type();
    }
}

public record IRFieldLoad(int line, IRField field) implements IRExpression {
    public IRFieldLoad {
        Objects.requireNonNull(field, "field");
    }

    @Override
    public RuntimeType type() {
        return field.type();
    }
}

public record IRLocalStore(int line, IRLocal local, IRExpression value) implements IRExpression {
    public IRLocalStore {
        Objects.requireNonNull(local, "local");
        Objects.requireNonNull(value, "value");
    }

    @Override
    public RuntimeType type() {
        return local.type();
    }
}

public record IRFieldStore(int line, IRField field, IRExpression value) implements IRExpression {
    public IRFieldStore {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(value, "value");
    }

    @Override
    public RuntimeType type() {
        return field.type();
    }
}
