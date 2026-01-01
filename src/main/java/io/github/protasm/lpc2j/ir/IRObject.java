package io.github.protasm.lpc2j.ir;

import java.util.List;
import java.util.Objects;

public record IRObject(int line, String name, String parentInternalName, List<IRField> fields, List<IRMethod> methods) {
    public IRObject {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(parentInternalName, "parentInternalName");
        fields = List.copyOf(fields);
        methods = List.copyOf(methods);
    }
}
