package io.github.protasm.lpc2j.semantic;

import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.Map;

/** Resolves LPC type keyword strings to {@link LPCType} values. */
public final class TypeResolver {
    private static final Map<String, LPCType> TYPE_KEYWORDS =
            Map.of(
                    "int", LPCType.LPCINT,
                    "float", LPCType.LPCFLOAT,
                    "mapping", LPCType.LPCMAPPING,
                    "mixed", LPCType.LPCMIXED,
                    "object", LPCType.LPCOBJECT,
                    "status", LPCType.LPCSTATUS,
                    "string", LPCType.LPCSTRING,
                    "void", LPCType.LPCVOID);

    public LPCType resolve(String typeName) {
        if (typeName == null)
            return null;

        return TYPE_KEYWORDS.get(typeName.toLowerCase());
    }
}
