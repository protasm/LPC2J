package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.List;

public final class EfunSetHeartBeat implements Efun {
    public static final EfunSetHeartBeat INSTANCE = new EfunSetHeartBeat();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCVOID, "set_heart_beat"), List.of(LPCType.LPCSTATUS));

    private EfunSetHeartBeat() {
    }

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public Object call(RuntimeContext context, Object[] args) {
        // TODO: Implement heart beat control when runtime support is available
        return null;
    }
}
