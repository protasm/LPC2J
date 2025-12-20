package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.List;

public final class EfunEnvironment implements Efun {
    public static final EfunEnvironment INSTANCE = new EfunEnvironment();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCOBJECT, "environment"), List.of(LPCType.LPCOBJECT));

    private EfunEnvironment() {
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
