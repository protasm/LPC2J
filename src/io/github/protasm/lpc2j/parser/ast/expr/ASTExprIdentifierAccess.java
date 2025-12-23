package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.IdentifierResolution;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.Objects;

/** Identifier reference whose resolution is deferred to semantic analysis. */
public final class ASTExprIdentifierAccess extends ASTExpression {
    private final String name;
    private IdentifierResolution resolution;
    private LPCType lpcType;

    public ASTExprIdentifierAccess(int line, String name) {
        super(line);
        this.name = Objects.requireNonNull(name, "name");
        this.lpcType = LPCType.LPCMIXED;
    }

    public String name() {
        return name;
    }

    public IdentifierResolution resolution() {
        return resolution;
    }

    public void setResolution(IdentifierResolution resolution) {
        this.resolution = resolution;
    }

    public void setLpcType(LPCType lpcType) {
        this.lpcType = lpcType;
    }

    @Override
    public LPCType lpcType() {
        if (lpcType != null)
            return lpcType;

        if (resolution != null)
            return resolution.lpcType();

        return LPCType.LPCMIXED;
    }
}
