package io.github.protasm.lpc2j.ir;

import java.util.List;
import java.util.Objects;

public record IRBlock(String label, List<IRStatement> statements, IRTerminator terminator) {
    public IRBlock {
        Objects.requireNonNull(label, "label");
        statements = List.copyOf(statements);
        Objects.requireNonNull(terminator, "terminator");
    }
}
