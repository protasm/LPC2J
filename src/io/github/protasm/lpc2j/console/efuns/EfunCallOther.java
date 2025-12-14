package io.github.protasm.lpc2j.console.efuns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunCallOther implements Efun {
    public static final EfunCallOther INSTANCE = new EfunCallOther();
    private static final Symbol SYM = new Symbol(LPCType.LPCMIXED, "call_other");

    private EfunCallOther() {
    }

    @Override
    public Symbol symbol() {
        return SYM;
    }

    @Override
    public int arity() {
        return 2;
    }

    @Override
    public Object invoke(Object[] args) {
        Object[] a = (args == null) ? new Object[0] : args;

        if (a.length < 2)
            throw new IllegalArgumentException(
                    "efun '" + symbol().name() + "' expects at least 2 arg(s); got " + a.length);

        return call(a);
    }

    @Override
    public Object call(Object[] args) {
        Object target = args[0];
        String methodName = String.valueOf(args[1]);
        Object[] methodArgs = (args.length > 2) ? Arrays.copyOfRange(args, 2, args.length) : new Object[0];

        if (target == null)
            throw new IllegalArgumentException("efun '" + symbol().name() + "' requires a target object");

        Method method = findMethod(target.getClass(), methodName, methodArgs);

        if (method == null)
            throw new IllegalArgumentException("No matching method '" + methodName + "' found on "
                    + target.getClass().getName());

        try {
            return method.invoke(target, methodArgs);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to invoke method '" + methodName + "'", e);
        }
    }

    private Method findMethod(Class<?> clazz, String methodName, Object[] args) {
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (!method.getName().equals(methodName))
                continue;

            if (method.getParameterCount() != args.length)
                continue;

            Class<?>[] parameterTypes = method.getParameterTypes();
            boolean compatible = true;

            for (int i = 0; i < parameterTypes.length; i++) {
                if (!isCompatible(parameterTypes[i], args[i])) {
                    compatible = false;
                    break;
                }
            }

            if (compatible)
                return method;
        }

        return null;
    }

    private boolean isCompatible(Class<?> parameterType, Object arg) {
        if (arg == null)
            return !parameterType.isPrimitive();

        if (parameterType.isInstance(arg))
            return true;

        if (parameterType.isPrimitive())
            return isWrapperOfPrimitive(parameterType, arg.getClass());

        return false;
    }

    private boolean isWrapperOfPrimitive(Class<?> primitive, Class<?> wrapper) {
        if (primitive == boolean.class)
            return wrapper == Boolean.class;
        if (primitive == byte.class)
            return wrapper == Byte.class;
        if (primitive == short.class)
            return wrapper == Short.class;
        if (primitive == int.class)
            return wrapper == Integer.class;
        if (primitive == long.class)
            return wrapper == Long.class;
        if (primitive == float.class)
            return wrapper == Float.class;
        if (primitive == double.class)
            return wrapper == Double.class;
        if (primitive == char.class)
            return wrapper == Character.class;

        return false;
    }
}
