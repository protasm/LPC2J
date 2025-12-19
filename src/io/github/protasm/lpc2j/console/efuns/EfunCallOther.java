package io.github.protasm.lpc2j.console.efuns;

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
        Object[] safeArgs = (args == null) ? new Object[0] : args;
        return call(safeArgs);
    }

    @Override
    public Object call(Object[] args) {
        return null;
    }
}
