package io.github.protasm.lpc2j;

import java.lang.reflect.Method;

public class LPCObject {
    public LPCObject() {
	System.out.println("Foo");
    }

    /*
    public Object dispatch(String methodName, Object... args) {
	System.out.println("Attempting to dispatch '" + methodName + "'....");

	try {
	    // Get the runtime class
	    Class<?> clazz = this.getClass();

	    // Determine the argument types
	    Class<?>[] argTypes = new Class<?>[args.length];

	    for (int i = 0; i < args.length; i++)
		argTypes[i] = args[i].getClass();

	    // Look for the method with the given name and argument types
	    Method method = clazz.getMethod(methodName, argTypes);

	    // Invoke the method on this instance
	    return method.invoke(this, args);
	} catch (NoSuchMethodException e) {
	    throw new IllegalArgumentException("Method not found: " + methodName, e);
	} catch (Exception e) {
	    throw new RuntimeException("Failed to invoke method: " + methodName, e);
	}
    }
    */
    
    public Object dispatch(String methodName, Integer arg) {
	System.out.println("Attempting to simple dispatch method '" + methodName + "'....");

	try {
	    // Get the runtime class
	    Class<?> clazz = this.getClass();

	    // Look for the method with the given name and argument types
	    Method method = clazz.getMethod(methodName, Integer.class);

	    // Invoke the method on this instance
	    return method.invoke(this, arg);
	} catch (NoSuchMethodException e) {
	    throw new IllegalArgumentException("Method not found: " + methodName, e);
	} catch (Exception e) {
	    throw new RuntimeException("Failed to invoke method: " + methodName, e);
	}
    }
}
