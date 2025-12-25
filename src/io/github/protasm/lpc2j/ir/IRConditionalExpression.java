package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public record IRConditionalExpression(
        int line,
        IRExpression condition,
        IRExpression thenBranch,
        IRExpression elseBranch,
        RuntimeType type)
        implements IRExpression {
    public IRConditionalExpression {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(thenBranch, "thenBranch");
        Objects.requireNonNull(elseBranch, "elseBranch");
        Objects.requireNonNull(type, "type");
    }
}
