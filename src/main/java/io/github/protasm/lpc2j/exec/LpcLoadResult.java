package io.github.protasm.lpc2j.exec;

import java.util.Objects;
import java.util.Optional;

/**
 * Result wrapper for loading LPC objects, allowing callers to handle failures gracefully.
 */
public final class LpcLoadResult {
    private final LpcObjectHandle handle;
    private final Throwable error;

    private LpcLoadResult(LpcObjectHandle handle, Throwable error) {
        this.handle = handle;
        this.error = error;
    }

    public static LpcLoadResult success(LpcObjectHandle handle) {
        return new LpcLoadResult(Objects.requireNonNull(handle, "handle"), null);
    }

    public static LpcLoadResult failure(Throwable error) {
        return new LpcLoadResult(null, Objects.requireNonNull(error, "error"));
    }

    public boolean succeeded() {
        return handle != null;
    }

    public Optional<LpcObjectHandle> handle() {
        return Optional.ofNullable(handle);
    }

    public Optional<Throwable> error() {
        return Optional.ofNullable(error);
    }
}
