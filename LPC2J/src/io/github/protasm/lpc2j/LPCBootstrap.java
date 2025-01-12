package io.github.protasm.lpc2j;

import java.lang.invoke.*;

public class LPCBootstrap {
    public static CallSite bootstrap(MethodHandles.Lookup lookup, String methodName, MethodType methodType)
	    throws Throwable {
	// Dynamically resolve the target method on the object
	MethodHandle target = lookup.findVirtual(Object.class, methodName, methodType);

	return new ConstantCallSite(target);
    }
}
