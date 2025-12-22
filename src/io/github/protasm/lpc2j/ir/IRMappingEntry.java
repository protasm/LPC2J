package io.github.protasm.lpc2j.ir;

import java.util.Objects;

public record IRMappingEntry(IRExpression key, IRExpression value) {
    public IRMappingEntry {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }
}
