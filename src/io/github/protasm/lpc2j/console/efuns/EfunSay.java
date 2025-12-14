package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunSay implements Efun {
    public static final EfunSay INSTANCE = new EfunSay();
    private static final Symbol SYM = new Symbol(LPCType.LPCVOID, "say");

    private EfunSay() {
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

        if (a.length != 1 && a.length != 2)
            throw new IllegalArgumentException(
                    "efun '" + symbol().name() + "' expects 1 or 2 arg(s); got " + a.length);

        return call(a);
    }

    @Override
    public Object call(Object[] args) {
        if (args.length == 0)
            return null;

        String message = String.valueOf(args[0]);

        // TODO: Broadcast to players sharing the caller's environment instead of stdout.
        System.out.println(message);

        return null;
    }
}
