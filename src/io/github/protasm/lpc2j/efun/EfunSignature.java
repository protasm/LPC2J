package io.github.protasm.lpc2j.efun;

import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.List;
import java.util.Objects;

/** Describes the contract for an efun: its name, return type, and parameter types. */
public record EfunSignature(Symbol symbol, List<LPCType> parameterTypes) {
    public EfunSignature {
        Objects.requireNonNull(symbol, "symbol");
        parameterTypes = List.copyOf(parameterTypes != null ? parameterTypes : List.of());
    }

    public String name() {
        return symbol.name();
    }

    public LPCType returnType() {
        return symbol.lpcType();
    }

    public int arity() {
        return parameterTypes.size();
    }
}
