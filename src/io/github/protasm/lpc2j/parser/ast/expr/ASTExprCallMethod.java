package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprCallMethod extends ASTExpression {
    private final ASTMethod method;
    private final ASTArguments arguments;

    public ASTExprCallMethod(int line, ASTMethod method, ASTArguments arguments) {
        super(line);

        this.method = method;
        this.arguments = arguments;
    }

    public ASTMethod method() {
        return method;
    }

    public ASTArguments arguments() {
        return arguments;
    }

    @Override
    public LPCType lpcType() {
        return method.symbol().lpcType();
    }
}
