package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.IdentifierResolution;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.Objects;

/** Call expression that will be bound to a method or efun during semantic analysis. */
public final class ASTExprIdentifierCall extends ASTExpression {
    private final String name;
    private final ASTArguments arguments;
    private IdentifierResolution resolution;
    private LPCType lpcType;

    public ASTExprIdentifierCall(int line, String name, ASTArguments arguments) {
        super(line);
        this.name = Objects.requireNonNull(name, "name");
        this.arguments = Objects.requireNonNull(arguments, "arguments");
    }

    public String name() {
        return name;
    }

    public ASTArguments arguments() {
        return arguments;
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
