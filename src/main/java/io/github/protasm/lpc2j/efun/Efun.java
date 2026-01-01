package io.github.protasm.lpc2j.efun;

import io.github.protasm.lpc2j.runtime.RuntimeContext;
import io.github.protasm.lpc2j.parser.ast.Symbol;

public interface Efun {
    EfunSignature signature();

    Object call(RuntimeContext context, Object[] args);

    default Symbol symbol() {
        return signature().symbol();
    }

    default int arity() {
        return signature().arity();
    }

    default Object invoke(RuntimeContext context, Object[] args) {
        Object[] a = (args == null) ? new Object[0] : args;

        if (a.length != arity())
            throw new IllegalArgumentException(
                    "efun '" + symbol().name() + "' expects " + arity() + " arg(s); got " + a.length);

        return call(context, a);
    }
}
