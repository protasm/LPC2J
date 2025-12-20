package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprCallEfun extends ASTExpression {
    private final Efun efun;
    private final ASTArguments arguments;

    public ASTExprCallEfun(int line, Efun efun, ASTArguments arguments) {
        super(line);

        this.efun = efun;
        this.arguments = arguments;
    }

    public Efun efun() {
        return efun;
    }

    public ASTArguments arguments() {
        return arguments;
    }

    @Override
    public LPCType lpcType() {
        return efun.symbol().lpcType();
    }
}
