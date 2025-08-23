package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprCallEfun extends ASTExpression {
    private final Efun efun;
    private final ASTArguments arguments;

    public ASTExprCallEfun(int line, Efun efun, ASTArguments arguments) {
        super(line);

        this.efun = efun;
        this.arguments = arguments;
    }

    public Efun efun() { return efun; }
    public ASTArguments arguments() { return arguments; }

    @Override
    public LPCType lpcType() {
        return efun.symbol().lpcType();
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
