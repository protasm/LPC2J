package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EfunKeys implements Efun {
    public static final EfunKeys INSTANCE = new EfunKeys();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCARRAY, "keys"), List.of(LPCType.LPCMAPPING));

    private EfunKeys() {
    }

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public Object call(RuntimeContext context, Object[] args) {
        if (args == null || args.length != 1) {
            return List.of();
        }

        Object value = args[0];
        if (!(value instanceof Map<?, ?> map)) {
            return List.of();
        }

        return new ArrayList<>(map.keySet());
    }
}
