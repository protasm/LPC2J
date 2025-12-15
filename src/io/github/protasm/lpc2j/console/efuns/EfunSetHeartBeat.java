package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunSetHeartBeat implements Efun {
    public static final EfunSetHeartBeat INSTANCE = new EfunSetHeartBeat();
    private static final Symbol SYM = new Symbol(LPCType.LPCVOID, "set_heart_beat");

    private EfunSetHeartBeat() {
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
        // TODO: Implement heart beat control when runtime support is available
        return null;
    }
}
