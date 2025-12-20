package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.List;

public final class EfunAddVerb implements Efun {
    public static final EfunAddVerb INSTANCE = new EfunAddVerb();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCVOID, "add_verb"), List.of(LPCType.LPCMIXED));

    private EfunAddVerb() {
    }

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public Object call(RuntimeContext context, Object[] args) {
        // TODO: Implement verb registration when command handling is available
        return null;
    }
}
