package io.github.protasm.lpc2j.parser.ast.expr;

import static io.github.protasm.lpc2j.parser.type.LPCType.LPCINT;
import static io.github.protasm.lpc2j.parser.type.LPCType.LPCSTATUS;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;

public class ASTExprOpUnary extends ASTExpression {
    private final ASTExpression right;
    private final UnaryOpType operator;

    public ASTExprOpUnary(int line, ASTExpression right, UnaryOpType operator) {
        super(line);

        this.right = right;
        this.operator = operator;
    }

    public ASTExpression right() {
        return right;
    }

    public UnaryOpType operator() {
        return operator;
    }

    @Override
    public LPCType lpcType() {
        switch (operator) {
        case UOP_NEGATE:
            if (right.lpcType() == LPCINT)
                return LPCINT;
            else
                throw new IllegalStateException("Unary '-' operator requires an integer operand.");
        case UOP_NOT:
            return LPCSTATUS;
        }

        return null; // unreachable
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
