package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.Objects;

/** Carries the semantic target for an identifier once it has been resolved. */
public final class IdentifierResolution {
    public enum Kind {
        LOCAL,
        FIELD,
        METHOD,
        EFUN
    }

    private final Kind kind;
    private final ASTLocal local;
    private final ASTField field;
    private final ASTMethod method;
    private final Efun efun;

    private IdentifierResolution(Kind kind, ASTLocal local, ASTField field, ASTMethod method, Efun efun) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.local = local;
        this.field = field;
        this.method = method;
        this.efun = efun;
    }

    public static IdentifierResolution forLocal(ASTLocal local) {
        return new IdentifierResolution(Kind.LOCAL, Objects.requireNonNull(local, "local"), null, null, null);
    }

    public static IdentifierResolution forField(ASTField field) {
        return new IdentifierResolution(Kind.FIELD, null, Objects.requireNonNull(field, "field"), null, null);
    }

    public static IdentifierResolution forMethod(ASTMethod method) {
        return new IdentifierResolution(Kind.METHOD, null, null, Objects.requireNonNull(method, "method"), null);
    }

    public static IdentifierResolution forEfun(Efun efun) {
        return new IdentifierResolution(Kind.EFUN, null, null, null, Objects.requireNonNull(efun, "efun"));
    }

    public Kind kind() {
        return kind;
    }

    public ASTLocal local() {
        return local;
    }

    public ASTField field() {
        return field;
    }

    public ASTMethod method() {
        return method;
    }

    public Efun efun() {
        return efun;
    }

    public String name() {
        return switch (kind) {
        case LOCAL -> local.symbol().name();
        case FIELD -> field.symbol().name();
        case METHOD -> method.symbol().name();
        case EFUN -> efun.signature().name();
        };
    }

    public LPCType lpcType() {
        return switch (kind) {
        case LOCAL -> local.symbol().lpcType();
        case FIELD -> field.symbol().lpcType();
        case METHOD -> method.symbol().lpcType();
        case EFUN -> efun.signature().returnType();
        };
    }
}
