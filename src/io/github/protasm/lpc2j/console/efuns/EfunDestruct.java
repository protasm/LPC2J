package io.github.protasm.lpc2j.console.efuns;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunDestruct implements Efun {
    public static final EfunDestruct INSTANCE = new EfunDestruct();
    private static final Symbol SYM = new Symbol(LPCType.LPCVOID, "destruct");

    private EfunDestruct() {
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
    public Object call(Object[] args) {
        Object target = args[0];

        if (target == null)
            return null;

        tryInvokeDestruct(target);
        tryInvokeClose(target);

        return null;
    }

    private void tryInvokeDestruct(Object target) {
        try {
            Method destructMethod = target.getClass().getMethod("destruct");
            destructMethod.invoke(target);
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to destruct object", e);
        }
    }

    private void tryInvokeClose(Object target) {
        if (!(target instanceof AutoCloseable closeable))
            return;

        try {
            closeable.close();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to close object during destruct", e);
        }
    }
}
