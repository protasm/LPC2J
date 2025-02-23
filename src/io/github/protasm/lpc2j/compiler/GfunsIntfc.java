package io.github.protasm.lpc2j.compiler;

import java.lang.reflect.Method;
import java.util.List;

public interface GfunsIntfc {
    // Retrieve multiple overloaded methods (primary function lookup)
    List<Method> getMethods(String name);

    default boolean hasMethod(String name) {
	return getMethod(name) != null;
    }

    // Retrieve a single method (defaulting to first match)
    default Method getMethod(String name) {
	List<Method> methods = getMethods(name);

	return methods.isEmpty() ? null : methods.get(0);
    }

    // Register functions dynamically
    void register(String name, Object instance, Method method);

    // Convenience method to register using method's name
    default void register(Object instance, Method method) {
	register(method.getName(), instance, method);
    }
}
