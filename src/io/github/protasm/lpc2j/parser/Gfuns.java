package io.github.protasm.lpc2j.parser;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Gfuns {
    private static final Map<String, Method> GFUN_MAP = new HashMap<>();

    static {
	try {
	    // Register all global functions
	    GFUN_MAP.put("first_inventory", Gfuns.class.getMethod("first_inventory", Object.class));
	    GFUN_MAP.put("this_object", Gfuns.class.getMethod("this_object"));
	    GFUN_MAP.put("write", Gfuns.class.getMethod("write", String.class));
	    GFUN_MAP.put("add_action", Gfuns.class.getMethod("add_action", String.class, String.class));
	} catch (NoSuchMethodException e) {
	    throw new RuntimeException("Failed to register global functions", e);
	}
    }

    public static Method getGfun(String name) {
	return GFUN_MAP.get(name);
    }

    public static void write(String message) {
	System.out.println(message);
    }
    
    public static void add_action(String str1, String str2) {
	
    }
    
    public static boolean first_inventory(Object obj) {
	return false;
    }
    
    public static Object this_object() {
	return new Object();
    }
}
