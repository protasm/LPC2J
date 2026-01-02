package io.github.protasm.lpc2j.exec;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

/** Handle to a compiled LPC object instance managed by an {@link LpcRuntime}. */
public final class LpcObjectHandle {
    private final LpcRuntime runtime;
    private final String internalName;
    private final Class<?> objectClass;
    private final Object instance;

    LpcObjectHandle(LpcRuntime runtime, String internalName, Class<?> objectClass, Object instance) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.internalName = Objects.requireNonNull(internalName, "internalName");
        this.objectClass = Objects.requireNonNull(objectClass, "objectClass");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    public String internalName() {
        return internalName;
    }

    public Class<?> objectClass() {
        return objectClass;
    }

    public Object instance() {
        return instance;
    }

    public <T> T withRuntimeContext(Supplier<T> action) {
        return runtime.withRuntimeContext(action);
    }

    public void runWithRuntimeContext(Runnable action) {
        runtime.runWithRuntimeContext(action);
    }

    public String getClassName() {
      return this.instance().getClass().getName();
    }

    public Object invoke(String methodName) {
        return runtime.withCurrentObject(instance, () -> {
            try {
                Method method = this.objectClass().getMethod(methodName);

                return method.invoke(this.instance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Object invoke(String methodName, Object... args) {
    Objects.requireNonNull(methodName, "methodName");

    return runtime.withCurrentObject(instance, () -> {
        try {
            Method method = findMethod(methodName, args.length);
            return method.invoke(instance, args);

        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to invoke LPC method '" + methodName + "'", e
            );
        }
    });
    }

    public void invokeVoid(String methodName, Object... args) {
    runtime.runWithCurrentObject(instance, () -> {
        invoke(methodName, args);
    });
    }

    private Method findMethod(String name, int arity) {
    for (Method m : objectClass().getMethods()) {
        if (m.getName().equals(name)
            && m.getParameterCount() == arity) {

            m.setAccessible(true);
            return m;
        }
    }

    throw new IllegalArgumentException(
        "No LPC method named '" + name + "' with " + arity + " arguments"
    );
    }
}
