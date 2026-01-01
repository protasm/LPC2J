package io.github.protasm.lpc2j.ir;

import java.util.Objects;

public record TypedIR(IRObject object) {
    public TypedIR {
        Objects.requireNonNull(object, "object");
    }
}
