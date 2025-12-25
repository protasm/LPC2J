package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprInvokeField extends ASTExpression {
    private LPCType lpcType;
    private final ASTField field;
    private final String methodName;
    private final ASTArguments args;

    public ASTExprInvokeField(int line, ASTField field, String methodName, ASTArguments args) {
        super(line);

        this.lpcType = null; // set in type inference pass
        this.field = field;
        this.methodName = methodName;
        this.args = args;
    }

    public ASTField field() {
        return field;
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
