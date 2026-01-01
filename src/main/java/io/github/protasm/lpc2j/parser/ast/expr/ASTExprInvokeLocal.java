package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprInvokeLocal extends ASTExpression {
    private LPCType lpcType;
    private final ASTLocal local;
    private final String methodName;
    private final ASTArguments args;

    public ASTExprInvokeLocal(int line, ASTLocal local, String methodName, ASTArguments args) {
        super(line);

        this.lpcType = null; // set in type inference pass
        this.local = local;
        this.methodName = methodName;
        this.args = args;
    }

    public ASTLocal local() {
        return local;
    }

    public Integer slot() {
        return (local != null) ? local.slot() : null;
    }

    public String methodName() {
        return methodName;
    }

    public ASTArguments args() {
        return args;
    }

    /** Prefer {@link #arguments()} for consistency with other call expressions. */
    public ASTArguments arguments() {
        return args;
    }

    @Override
    public LPCType lpcType() {
        return lpcType;
    }

    public void setLPCType(LPCType lpcType) {
        this.lpcType = lpcType;
    }
}
