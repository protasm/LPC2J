package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.List;

public final class EfunSetLight implements Efun {
    public static final EfunSetLight INSTANCE = new EfunSetLight();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCINT, "set_light"), List.of(LPCType.LPCINT));

    private EfunSetLight() {
    }

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public synchronized Object call(RuntimeContext context, Object[] args) {
        return null;
    }
}
