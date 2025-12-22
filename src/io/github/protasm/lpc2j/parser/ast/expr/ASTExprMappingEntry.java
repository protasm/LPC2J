package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import java.util.Objects;

public final class ASTExprMappingEntry {
    private final ASTExpression key;
    private final ASTExpression value;

    public ASTExprMappingEntry(ASTExpression key, ASTExpression value) {
        this.key = Objects.requireNonNull(key, "key");
        this.value = Objects.requireNonNull(value, "value");
    }

    public ASTExpression key() {
        return key;
    }

    public ASTExpression value() {
        return value;
    }
}
