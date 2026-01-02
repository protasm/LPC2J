package io.github.protasm.lpc2j.exec;

import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import java.util.List;

/** Signals failures while compiling or instantiating LPC objects for a runtime instance. */
public final class LpcRuntimeException extends RuntimeException {
    private final List<CompilationProblem> problems;

    public LpcRuntimeException(String message) {
        super(message);
        this.problems = List.of();
    }

    public LpcRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.problems = List.of();
    }

    public LpcRuntimeException(String message, List<CompilationProblem> problems) {
        super(message);
        this.problems = (problems != null) ? List.copyOf(problems) : List.of();
    }

    public List<CompilationProblem> problems() {
        return problems;
    }
}
