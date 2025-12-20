package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record IRMethod(
        int line,
        String name,
        RuntimeType returnType,
        List<IRParameter> parameters,
        List<IRLocal> locals,
        List<IRBlock> blocks,
        String entryBlockLabel) {
    public IRMethod {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(returnType, "returnType");
        parameters = List.copyOf(parameters);
        locals = List.copyOf(locals);
        blocks = List.copyOf(blocks);
        Objects.requireNonNull(entryBlockLabel, "entryBlockLabel");
    }

    public Optional<IRBlock> entryBlock() {
        return blocks.stream().filter(block -> block.label().equals(entryBlockLabel)).findFirst();
    }
}
