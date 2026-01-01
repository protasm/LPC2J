package io.github.protasm.lpc2j.ir;

import java.util.Objects;

public record IRJump(int line, String targetLabel) implements IRTerminator {
    public IRJump {
        Objects.requireNonNull(targetLabel, "targetLabel");
    }
}
