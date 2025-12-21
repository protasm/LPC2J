package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;

public final class ASTStmtReturn extends ASTStatement {
    private final ASTExpression returnValue;
    private final boolean synthetic;

    public ASTStmtReturn(int line, ASTExpression returnValue) {
        this(line, returnValue, false);
    }

    public ASTStmtReturn(int line, ASTExpression returnValue, boolean synthetic) {
        super(line);
        this.returnValue = returnValue;
        this.synthetic = synthetic;
    }

    public ASTExpression returnValue() {
        return returnValue;
    }

    public boolean isSynthetic() {
        return synthetic;
    }
}
