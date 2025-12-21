package io.github.protasm.lpc2j.pipeline;

import java.util.Objects;

public final class CompilationProblem {
    private final CompilationStage stage;
    private final String message;
    private final Integer line;
    private final Throwable throwable;

    public CompilationProblem(CompilationStage stage, String message) {
        this(stage, message, null, null);
    }

    public CompilationProblem(CompilationStage stage, String message, Throwable throwable) {
        this(stage, message, null, throwable);
    }

    public CompilationProblem(CompilationStage stage, String message, Integer line) {
        this(stage, message, line, null);
    }

    public CompilationProblem(CompilationStage stage, String message, Integer line, Throwable throwable) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.message = Objects.requireNonNull(message, "message");
        this.line = line;
        this.throwable = throwable;
    }

    public CompilationStage getStage() {
        return stage;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Integer getLine() {
        return line;
    }
}
