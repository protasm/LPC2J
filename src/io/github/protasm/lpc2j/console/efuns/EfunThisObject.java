package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.console.RuntimeContext;
import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunThisObject implements Efun {
    public static final EfunThisObject INSTANCE = new EfunThisObject();
    private static final Symbol SYM = new Symbol(LPCType.LPCOBJECT, "this_object");

    private EfunThisObject() {
    }

    @Override
    public Symbol symbol() {
        return SYM;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Object[] args) {
        return RuntimeContext.getCurrentObject();
    }
}
