package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprUnresolvedParentCall extends ASTExpression {
    private final String name;
    private final ASTArguments arguments;

    public ASTExprUnresolvedParentCall(int line, String name, ASTArguments arguments) {
        super(line);

        this.name = name;
        this.arguments = arguments;
    }

    public String name() {
        return name;
    }

    public ASTArguments arguments() {
        return arguments;
    }

    @Override
    public LPCType lpcType() {
        return LPCType.LPCMIXED;
    }
}
