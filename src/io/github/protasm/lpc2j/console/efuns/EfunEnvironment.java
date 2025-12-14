package io.github.protasm.lpc2j.console.efuns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunEnvironment implements Efun {
    public static final EfunEnvironment INSTANCE = new EfunEnvironment();
    private static final Symbol SYM = new Symbol(LPCType.LPCOBJECT, "environment");

    private EfunEnvironment() {
    }

    @Override
    public Symbol symbol() {
        return SYM;
    }

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Object invoke(Object[] args) {
        Object[] a = (args == null) ? new Object[0] : args;

        if (a.length != 0 && a.length != 1)
            throw new IllegalArgumentException(
                    "efun '" + symbol().name() + "' expects 0 or 1 arg(s); got " + a.length);

        return call(a);
    }

    @Override
    public Object call(Object[] args) {
        if (args.length == 0)
            return null;

        Object target = args[0];

        if (target == null)
            return null;

        try {
            Method method = target.getClass().getMethod("environment");
            return method.invoke(target);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to call environment() on object", e);
        }
    }
}
