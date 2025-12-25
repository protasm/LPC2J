package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunSignature;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class EfunExplode implements Efun {
    public static final EfunExplode INSTANCE = new EfunExplode();
    private static final EfunSignature SIGNATURE =
            new EfunSignature(new Symbol(LPCType.LPCARRAY, "explode"),
                    List.of(LPCType.LPCSTRING, LPCType.LPCSTRING));

    private EfunExplode() {
    }

    @Override
    public EfunSignature signature() {
        return SIGNATURE;
    }

    @Override
    public Object call(RuntimeContext context, Object[] args) {
        if (args == null || args.length != 2) {
            return List.of();
        }

        Object value = args[0];
        Object delimiter = args[1];
        if (!(value instanceof String input) || !(delimiter instanceof String delim)) {
            return List.of();
        }

        if (delim.isEmpty()) {
            List<String> chars = new ArrayList<>(input.length());
            for (int i = 0; i < input.length(); i++) {
                chars.add(String.valueOf(input.charAt(i)));
            }
            return chars;
        }

        String[] parts = input.split(Pattern.quote(delim), -1);
        List<String> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            result.add(part);
        }
        return result;
    }
}
