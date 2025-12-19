package io.github.protasm.lpc2j.console.efuns;

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
        return null;
    }
}
