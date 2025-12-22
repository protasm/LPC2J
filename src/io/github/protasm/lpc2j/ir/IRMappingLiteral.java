package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IRMappingLiteral implements IRExpression {
    private final int line;
    private final List<IRMappingEntry> entries;
    private final RuntimeType type;

    public IRMappingLiteral(int line, List<IRMappingEntry> entries, RuntimeType type) {
        this.line = line;
        this.entries = new ArrayList<>(Objects.requireNonNull(entries, "entries"));
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public List<IRMappingEntry> entries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
