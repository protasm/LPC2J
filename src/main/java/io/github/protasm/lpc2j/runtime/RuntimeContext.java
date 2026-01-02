package io.github.protasm.lpc2j.runtime;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunRegistry;
import io.github.protasm.lpc2j.preproc.IncludeResolver;
import io.github.protasm.lpc2j.preproc.Preprocessor;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Encapsulates runtime state required by compiled LPC code.
 *
 * <p>This context owns efun registration, object lifecycle tracking, and include resolution
 * configuration so the compiler and runtime no longer depend on global singletons.</p>
 */
public final class RuntimeContext {
    private final EfunRegistry efunRegistry;
    private final IncludeResolver includeResolver;
    private final Map<String, Object> objects = new LinkedHashMap<>();
    private final ThreadLocal<Deque<Object>> currentObjectStack =
            ThreadLocal.withInitial(ArrayDeque::new);

    public RuntimeContext(IncludeResolver includeResolver) {
        this(includeResolver, new EfunRegistry());
    }

    public RuntimeContext(IncludeResolver includeResolver, EfunRegistry efunRegistry) {
        this.includeResolver = (includeResolver != null) ? includeResolver : Preprocessor.rejectingResolver();
        this.efunRegistry = Objects.requireNonNull(efunRegistry, "efunRegistry");
    }

    public IncludeResolver includeResolver() {
        return includeResolver;
    }

    public EfunRegistry efunRegistry() {
        return efunRegistry;
    }

    public Preprocessor newPreprocessor() {
        return new Preprocessor(includeResolver);
    }

    public void registerEfun(Efun efun) {
        efunRegistry.register(efun);
    }

    public Efun resolveEfun(String name, int arity) {
        return efunRegistry.lookup(name, arity);
    }

    public Object invokeEfun(String name, int arity, Object[] args) {
        Efun efun = efunRegistry.lookup(name, arity);

        if (efun == null)
            throw new IllegalArgumentException("Unknown efun '" + name + "' with arity " + arity);

        return efun.invoke(this, args);
    }

    public void registerObject(String name, Object object) {
        Objects.requireNonNull(name, "name");
        objects.put(name, object);
    }

    public Object getObject(String name) {
        return objects.get(name);
    }

    public Map<String, Object> objectsView() {
        return Collections.unmodifiableMap(objects);
    }

    public Map<String, Object> objects() {
        return objects;
    }

    public Object currentObject() {
        return currentObjectStack.get().peek();
    }

    public void pushCurrentObject(Object object) {
        Objects.requireNonNull(object, "object");
        currentObjectStack.get().push(object);
    }

    public void popCurrentObject() {
        Deque<Object> stack = currentObjectStack.get();
        if (stack.isEmpty()) {
            throw new IllegalStateException("No current LPC object is available to pop.");
        }
        stack.pop();
    }

    public <T> T withCurrentObject(Object object, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        pushCurrentObject(object);
        try {
            return action.get();
        } finally {
            popCurrentObject();
        }
    }

    public void runWithCurrentObject(Object object, Runnable action) {
        Objects.requireNonNull(action, "action");
        withCurrentObject(object, () -> {
            action.run();
            return null;
        });
    }
}
