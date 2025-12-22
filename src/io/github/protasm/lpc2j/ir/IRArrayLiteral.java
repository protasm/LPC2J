package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IRArrayLiteral implements IRExpression {
    private final int line;
    private final List<IRExpression> elements;
    private final RuntimeType type;

    public IRArrayLiteral(int line, List<IRExpression> elements, RuntimeType type) {
        this.line = line;
        this.elements = new ArrayList<>(Objects.requireNonNull(elements, "elements"));
        this.type = Objects.requireNonNull(type, "type");
    }

    @Override
    public int line() {
        return line;
    }

    public List<IRExpression> elements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public RuntimeType type() {
        return type;
    }
}
