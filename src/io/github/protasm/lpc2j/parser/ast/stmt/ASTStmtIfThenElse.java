package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTStmtIfThenElse extends ASTStatement {
    private final ASTExpression condition;
    private final ASTStatement thenBranch;
    private final ASTStatement elseBranch; // Nullable

    public ASTStmtIfThenElse(int line, ASTExpression condition, ASTStatement thenBranch, ASTStatement elseBranch) {
        super(line);

        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public ASTExpression condition() {
        return condition;
    }

    public ASTStatement thenBranch() {
        return thenBranch;
    }

    public ASTStatement elseBranch() {
        return elseBranch;
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
