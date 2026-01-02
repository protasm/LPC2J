package io.github.protasm.lpc2j.exec;

import java.util.HashMap;
import java.util.Map;

final class LpcRuntimeClassLoader extends ClassLoader {
    private final Map<String, Class<?>> definedClasses = new HashMap<>();

    LpcRuntimeClassLoader(ClassLoader parent) {
        super(parent);
    }

    synchronized Class<?> defineClass(String internalName, byte[] bytecode) {
        String binaryName = toBinaryName(internalName);
        Class<?> existing = definedClasses.get(binaryName);
        if (existing != null) {
            return existing;
        }

        Class<?> defined = defineClass(binaryName, bytecode, 0, bytecode.length);
        definedClasses.put(binaryName, defined);
        return defined;
    }

    synchronized boolean isDefined(String internalName) {
        return definedClasses.containsKey(toBinaryName(internalName));
    }

    @Override
    protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> defined = definedClasses.get(name);
        if (defined != null) {
            return defined;
        }
        throw new ClassNotFoundException(name);
    }

    private String toBinaryName(String internalName) {
        return internalName.replace('/', '.');
    }
}
