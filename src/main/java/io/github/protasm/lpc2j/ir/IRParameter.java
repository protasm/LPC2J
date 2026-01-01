package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public record IRParameter(int line, String name, RuntimeType type, IRLocal local) {
    public IRParameter {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(local, "local");
    }
}
