package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTStmtReturn extends ASTStatement {
    private final ASTExpression returnValue; // if any

    public ASTStmtReturn(int line, ASTExpression value) {
        super(line);

        this.returnValue = value;
    }

    public ASTExpression returnValue() {
        return returnValue;
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
