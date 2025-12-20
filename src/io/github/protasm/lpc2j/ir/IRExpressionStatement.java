package io.github.protasm.lpc2j.ir;

import java.util.Objects;

public record IRExpressionStatement(int line, IRExpression expression) implements IRStatement {
    public IRExpressionStatement {
        Objects.requireNonNull(expression, "expression");
    }
}
