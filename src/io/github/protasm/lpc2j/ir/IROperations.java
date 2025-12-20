package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;
import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.util.Objects;

public record IRUnaryOperation(int line, UnaryOpType operator, IRExpression operand, RuntimeType type)
        implements IRExpression {
    public IRUnaryOperation {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(operand, "operand");
        Objects.requireNonNull(type, "type");
    }
}

public record IRBinaryOperation(
        int line, BinaryOpType operator, IRExpression left, IRExpression right, RuntimeType type) implements IRExpression {
    public IRBinaryOperation {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");
        Objects.requireNonNull(type, "type");
    }
}

public record IRCoerce(int line, IRExpression value, RuntimeType targetType) implements IRExpression {
    public IRCoerce {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(targetType, "targetType");
    }

    @Override
    public RuntimeType type() {
        return targetType;
    }
}
