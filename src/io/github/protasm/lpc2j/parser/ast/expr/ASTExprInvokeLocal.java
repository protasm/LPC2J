package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprInvokeLocal extends ASTExpression {
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
        // called during Parser's type inference pass
        this.lpcType = lpcType;
    }

    @Override
    public void accept(Compiler visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
        visitor.visit(this, lpcType);
    }

    @Override
    public void accept(PrintVisitor visitor) {
        visitor.visit(this);
    }
}
