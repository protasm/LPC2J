package io.github.protasm.lpc2j;

import java.lang.reflect.Method;
import java.util.Arrays;

public class LPCObject {
    public LPCObject() {
	System.out.println("LPCObject constructor.");
    }

//    public Object dispatch(String methodName, Object[] args) {
//	System.out.println(
//		"Attempting to dispatch method '" + methodName + "' with args " + java.util.Arrays.toString(args));
//
//	try {
//	    Method method = this.getClass().getMethod(methodName, getParameterTypes(args));
//
//	    return method.invoke(this, args);
//	} catch (NoSuchMethodException e) {
//	    throw new RuntimeException("Method not found: " + methodName);
//	} catch (Exception e) {
//	    throw new RuntimeException("Error invoking method: " + methodName, e);
//	}
//    }
//
//    private Class<?>[] getParameterTypes(Object[] args) {
//	return Arrays.stream(args).map(arg -> arg != null ? arg.getClass() : Object.class).toArray(Class<?>[]::new);
//    }

    public Object dispatch(String methodName, Object... args) {
	System.out.println(
		"Attempting to dispatch method '" + methodName + "' with args " + java.util.Arrays.toString(args));

	try {
	    // Get the runtime class
	    Class<?> clazz = this.getClass();

	    // Determine the argument types dynamically
	    Class<?>[] argTypes = new Class<?>[args.length];

	    for (int i = 0; i < args.length; i++) {
		if (args[i] == null)
		    argTypes[i] = Object.class;
		else if (args[i] instanceof Integer)
		    argTypes[i] = int.class;
		else if (args[i] instanceof Boolean)
		    argTypes[i] = boolean.class;
		else if (args[i] instanceof Float)
		    argTypes[i] = float.class;
		else
		    argTypes[i] = args[i].getClass(); // Use the runtime class
	    }

	    // Look for the method with the given name and argument types
	    Method method = clazz.getMethod(methodName, argTypes);

	    // Invoke the method on this instance with the provided arguments
	    return method.invoke(this, args);
	} catch (NoSuchMethodException e) {
	    throw new IllegalArgumentException(
		    "Method not found: " + methodName + " with args: " + java.util.Arrays.toString(args), e);
	} catch (Exception e) {
	    throw new RuntimeException("Failed to invoke method: " + methodName, e);
	}
    }
}
