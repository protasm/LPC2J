package io.github.protasm.lpc2j.runtime;

import java.lang.reflect.Method;
import java.util.Arrays;

public class LPCObject {
	public Object dispatch(String methodName, Object... args) {
		try {
			// Get argument types dynamically
			Class<?>[] argTypes = new Class<?>[args.length];

			for (int i = 0; i < args.length; i++)
				argTypes[i] = (args[i] == null) ? Object.class : args[i].getClass();

			// Attempt to find a matching method
			Method method = findMethod(methodName, argTypes);

			if (method != null)
				return method.invoke(this, args);

			// If method is not found, handle it gracefully
			return missingMethod(methodName, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Method findMethod(String methodName, Class<?>[] argTypes) {
		Method[] methods = this.getClass().getMethods();

		for (Method method : methods)
			if (method.getName().equals(methodName))// && matchParameters(method.getParameterTypes(), argTypes))
				return method;

		return null;
	}

	private boolean matchParameters(Class<?>[] declaredTypes, Class<?>[] givenTypes) {
		if (declaredTypes.length != givenTypes.length)
			return false;

		for (int i = 0; i < declaredTypes.length; i++)
			if (!declaredTypes[i].isAssignableFrom(givenTypes[i]))
				return false;

		return true;
	}

	protected Object missingMethod(String methodName, Object... args) {
		System.err.println("Method '" + methodName + "' not found in " + this.getClass().getName() + " with args: "
				+ Arrays.toString(args));

		return null; // Default behavior: return null (could be changed to throw an exception)
	}
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

//    public Object dispatch(String methodName, Object... args) {
//	System.out.println(
//		"Attempting to dispatch method '" + methodName + "' with args " + java.util.Arrays.toString(args));
//
//	try {
//	    // Get the runtime class
//	    Class<?> clazz = this.getClass();
//
//	    // Determine the argument types dynamically
//	    Class<?>[] argTypes = new Class<?>[args.length];
//
//	    for (int i = 0; i < args.length; i++) {
//		if (args[i] == null)
//		    argTypes[i] = Object.class;
//		else if (args[i] instanceof Integer)
//		    argTypes[i] = int.class;
//		else if (args[i] instanceof Boolean)
//		    argTypes[i] = boolean.class;
//		else if (args[i] instanceof Float)
//		    argTypes[i] = float.class;
//		else
//		    argTypes[i] = args[i].getClass(); // Use the runtime class
//	    }
//
//	    // Look for the method with the given name and argument types
//	    Method method = clazz.getMethod(methodName, argTypes);
//
//	    // Invoke the method on this instance with the provided arguments
//	    return method.invoke(this, args);
//	} catch (NoSuchMethodException e) {
//	    throw new IllegalArgumentException(
//		    "Method not found: " + methodName + " with args: " + java.util.Arrays.toString(args), e);
//	} catch (Exception e) {
//	    throw new RuntimeException("Failed to invoke method: " + methodName, e);
//	}
//    }
