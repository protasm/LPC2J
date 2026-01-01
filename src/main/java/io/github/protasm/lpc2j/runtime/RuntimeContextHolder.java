package io.github.protasm.lpc2j.runtime;

/** Thread-local binding point for the active {@link RuntimeContext}. */
public final class RuntimeContextHolder {
    private static final ThreadLocal<RuntimeContext> CURRENT = new ThreadLocal<>();

    private RuntimeContextHolder() {}

    public static RuntimeContext current() {
        return CURRENT.get();
    }

    public static RuntimeContext requireCurrent() {
        RuntimeContext context = CURRENT.get();

        if (context == null)
            throw new IllegalStateException("RuntimeContext has not been installed for this thread.");

        return context;
    }

    public static void setCurrent(RuntimeContext context) {
        CURRENT.set(context);
    }
}
