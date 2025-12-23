package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprCallMethod extends ASTExpression {
    private final ASTMethod method;
    private final ASTArguments arguments;
    private final boolean parentDispatch;

    public ASTExprCallMethod(int line, ASTMethod method, ASTArguments arguments) {
        this(line, method, arguments, false);
    }

    public ASTExprCallMethod(int line, ASTMethod method, ASTArguments arguments, boolean parentDispatch) {
        super(line);

        this.method = method;
        this.arguments = arguments;
        this.parentDispatch = parentDispatch;
    }

    public ASTMethod method() {
        return method;
    }

    public ASTArguments arguments() {
        return arguments;
    }

    public boolean isParentDispatch() {
        return parentDispatch;
    }

    @Override
    public LPCType lpcType() {
        return method.symbol().lpcType();
    }
}
