package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.List;
import java.util.Map;

public final class EfunSizeof implements Efun {
    public static final EfunSizeof INSTANCE = new EfunSizeof();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCINT, "sizeof"), List.of(LPCType.LPCMIXED));

    private EfunSizeof() {
    }

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public Object call(RuntimeContext context, Object[] args) {
        if (args == null || args.length != 1) {
            return 0;
        }

        Object value = args[0];
        if (value == null) {
            return 0;
        }
        if (value instanceof String str) {
            return str.length();
        }
        if (value instanceof List<?> list) {
            return list.size();
        }
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        if (value instanceof Object[] array) {
            return array.length;
        }

        return 0;
    }
}
