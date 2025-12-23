package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.List;

public final class EfunWrite implements Efun {
    public static final EfunWrite INSTANCE = new EfunWrite();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCVOID, "write"), List.of(LPCType.LPCSTRING));

    private EfunWrite() {
    }

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public Object call(RuntimeContext context, Object[] args) {
        if (args.length < 1) return null;

        System.out.println(args[0]);

        return null;
    }
}
