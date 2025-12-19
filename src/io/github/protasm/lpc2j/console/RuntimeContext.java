package io.github.protasm.lpc2j.console;

/**
 * Thread-local runtime context for efuns that need information about the
 * currently executing LPC object.
 */
public final class RuntimeContext {
    private static final ThreadLocal<Object> CURRENT_OBJECT = new ThreadLocal<>();
    private static final ThreadLocal<Object> CURRENT_PLAYER = new ThreadLocal<>();

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

    public static Object getCurrentPlayer() {
        return CURRENT_PLAYER.get();
    }

    public static void setCurrentPlayer(Object obj) {
        CURRENT_PLAYER.set(obj);
    }

    public static void clearCurrentPlayer() {
        CURRENT_PLAYER.remove();
    }
}
