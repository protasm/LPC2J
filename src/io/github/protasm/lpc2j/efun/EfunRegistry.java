package io.github.protasm.lpc2j.efun;

import java.util.HashMap;
import java.util.Map;

public final class EfunRegistry {
    private static Map<String, Efun> registry = new HashMap<>();
    
    private EfunRegistry() {}
    
    public static void register(String name, Efun efun) {
	EfunRegistry.registry.put(name, efun);
    }

    public static Efun lookup(String name) {
	return EfunRegistry.registry.get(name);
    }
}
