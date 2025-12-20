package io.github.protasm.lpc2j.ir;

import java.util.Objects;

public record IRJump(int line, String targetLabel) implements IRTerminator {
    public IRJump {
        Objects.requireNonNull(targetLabel, "targetLabel");
    }
}

public record IRConditionalJump(int line, IRExpression condition, String trueLabel, String falseLabel)
        implements IRTerminator {
    public IRConditionalJump {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(trueLabel, "trueLabel");
        Objects.requireNonNull(falseLabel, "falseLabel");
    }
}

public record IRReturn(int line, IRExpression returnValue) implements IRTerminator {}
