package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class ASTExprLocalStore extends ASTExpression {
    private final ASTLocal local;
    private final ASTExpression value;
    private final boolean declarationInitializer;

    public ASTExprLocalStore(int line, ASTLocal local, ASTExpression value) {
        this(line, local, value, false);
    }

    public ASTExprLocalStore(int line, ASTLocal local, ASTExpression value, boolean declarationInitializer) {
        super(line);

        this.local = local;
        this.value = value;
        this.declarationInitializer = declarationInitializer;
    }

    public ASTLocal local() {
        return local;
    }

    public ASTExpression value() {
        return value;
    }

    public boolean isDeclarationInitializer() {
        return declarationInitializer;
    }

    @Override
    public LPCType lpcType() {
        return local.symbol().lpcType();
    }
}
