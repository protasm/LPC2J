package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.IdentifierResolution;
import io.github.protasm.lpc2j.parser.type.LPCType;
import java.util.Objects;

/** Dynamic invocation on an identifier, typically a local storing an object. */
public final class ASTExprInvokeIdentifier extends ASTExpression {
    private final String targetName;
    private final String methodName;
    private final ASTArguments arguments;
    private IdentifierResolution resolution;
    private LPCType lpcType;

    public ASTExprInvokeIdentifier(int line, String targetName, String methodName, ASTArguments arguments) {
        super(line);
        this.targetName = Objects.requireNonNull(targetName, "targetName");
        this.methodName = Objects.requireNonNull(methodName, "methodName");
        this.arguments = Objects.requireNonNull(arguments, "arguments");
    }

    public String targetName() {
        return targetName;
    }

    public String methodName() {
        return methodName;
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
