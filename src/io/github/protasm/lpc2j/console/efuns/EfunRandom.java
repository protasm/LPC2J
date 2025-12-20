package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class EfunRandom implements Efun {
    public static final EfunRandom INSTANCE = new EfunRandom();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCINT, "random"), List.of(LPCType.LPCINT));

    private EfunRandom() {}

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public Object call(RuntimeContext context, Object[] args) {
        if (args == null || args.length != 1)
            return 0;

        Object bound = args[0];
        if (!(bound instanceof Number))
            return 0;

        int limit = ((Number) bound).intValue();
        if (limit <= 0)
            return 0;

        return ThreadLocalRandom.current().nextInt(limit);
    }
}
