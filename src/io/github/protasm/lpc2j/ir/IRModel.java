package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record TypedIR(IRObject object) {
    public TypedIR {
        Objects.requireNonNull(object, "object");
    }
}

public record IRObject(int line, String name, String parentInternalName, List<IRField> fields, List<IRMethod> methods) {
    public IRObject {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(parentInternalName, "parentInternalName");
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
    }
}

public record IRField(int line, String name, RuntimeType type) {
    public IRField {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }
}

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

public record IRParameter(int line, String name, RuntimeType type, IRLocal local) {
    public IRParameter {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(local, "local");
    }
}

public record IRLocal(int line, String name, RuntimeType type, int slot, boolean parameter) {
    public IRLocal {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }
}

public record IRBlock(String label, List<IRStatement> statements, IRTerminator terminator) {
    public IRBlock {
        Objects.requireNonNull(label, "label");
        statements = List.copyOf(statements);
        Objects.requireNonNull(terminator, "terminator");
    }
}
