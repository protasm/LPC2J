package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunSetLight implements Efun {
    public static final EfunSetLight INSTANCE = new EfunSetLight();
    private static final Symbol SYM = new Symbol(LPCType.LPCINT, "set_light");

    private EfunSetLight() {
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
    public synchronized Object call(Object[] args) {
        return null;
    }
}
