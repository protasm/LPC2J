package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprInvokeLocal extends ASTExpression {
    private LPCType lpcType;
    private final Integer slot;
    private final String methodName;
    private final ASTArguments args;

    public ASTExprInvokeLocal(int line, int slot, String methodName, ASTArguments args) {
        super(line);

        this.lpcType = null; // set in type inference pass
        this.slot = slot;
        this.methodName = methodName;
        this.args = args;
    }

    public Integer slot() {
        return slot;
    }

    public String methodName() {
        return methodName;
    }

    public ASTArguments args() {
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
