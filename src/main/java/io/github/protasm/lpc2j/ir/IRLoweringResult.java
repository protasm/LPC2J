package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class IRLoweringResult {
    private final TypedIR typedIr;
    private final List<CompilationProblem> problems;

    public IRLoweringResult(TypedIR typedIr, List<CompilationProblem> problems) {
        this.typedIr = typedIr;
        this.problems = Collections.unmodifiableList(Objects.requireNonNull(problems, "problems"));
    }

    public boolean succeeded() {
        return typedIr != null && problems.isEmpty();
    }

    public TypedIR typedIr() {
        return typedIr;
    }

    public List<CompilationProblem> problems() {
        return problems;
    }
}
