package io.github.protasm.lpc2j;

import java.lang.invoke.*;

public class LPCBootstrap {
    public static CallSite bootstrap(MethodHandles.Lookup lookup, String methodName, MethodType methodType)
	        throws Throwable {
	    // Extract the runtime class of the receiver
	    Class<?> receiverClass = methodType.parameterType(0);

	    if (receiverClass == Object.class) {
	        throw new IllegalStateException("Receiver class cannot be java.lang.Object. Ensure a specific runtime type.");
	    }

	    // Dynamically resolve the method on the runtime receiver class
	    MethodHandle target = lookup.findVirtual(receiverClass, methodName, methodType.dropParameterTypes(0, 1));

	    // Return a CallSite with the resolved MethodHandle
	    return new ConstantCallSite(target);
	}

}
