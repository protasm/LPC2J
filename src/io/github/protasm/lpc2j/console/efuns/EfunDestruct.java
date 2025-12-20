package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.List;

public final class EfunDestruct implements Efun {
    public static final EfunDestruct INSTANCE = new EfunDestruct();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCVOID, "destruct"), List.of(LPCType.LPCOBJECT));

    private EfunDestruct() {
    }

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public Object call(RuntimeContext context, Object[] args) {
        return null;
    }
}
