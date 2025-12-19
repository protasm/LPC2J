package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunWrite implements Efun {
    public static final EfunWrite INSTANCE = new EfunWrite();
    private static final Symbol SYM = new Symbol(LPCType.LPCVOID, "write");

    private EfunWrite() {
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
