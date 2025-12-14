package io.github.protasm.lpc2j.efun;

import io.github.protasm.lpc2j.parser.ast.Symbol;

public interface Efun {
    Symbol symbol();

    int arity();

    Object call(Object[] args);

    default Object invoke(Object[] args) {
        Object[] a = (args == null) ? new Object[0] : args;

        if (a.length != arity())
            throw new IllegalArgumentException(
                    "efun '" + symbol().name() + "' expects " + arity() + " arg(s); got " + a.length);

        return call(a);
    }
}
