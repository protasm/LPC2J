package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.List;

public final class EfunLowerCase implements Efun {
    public static final EfunLowerCase INSTANCE = new EfunLowerCase();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCSTRING, "lower_case"), List.of(LPCType.LPCSTRING));

    private EfunLowerCase() {
    }

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public Object call(RuntimeContext context, Object[] args) {
        if (args == null || args.length != 1) {
            return "";
        }

        Object input = args[0];
        if (!(input instanceof String value)) {
            return "";
        }

        return value.toLowerCase();
    }
}
