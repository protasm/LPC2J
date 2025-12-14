package io.github.protasm.lpc2j.console;

/**
 * Thread-local runtime context for efuns that need information about the
 * currently executing LPC object.
 */
public final class RuntimeContext {
    private static final ThreadLocal<Object> CURRENT_OBJECT = new ThreadLocal<>();

    private RuntimeContext() {
    }

    public static Object getCurrentObject() {
        return CURRENT_OBJECT.get();
    }

    public static void setCurrentObject(Object obj) {
        CURRENT_OBJECT.set(obj);
    }

    public static void clearCurrentObject() {
        CURRENT_OBJECT.remove();
    }
}
