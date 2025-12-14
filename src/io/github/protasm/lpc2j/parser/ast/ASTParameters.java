package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTParameters extends ASTListNode<ASTParameter> {
    public ASTParameters(int line) {
        super(line);
    }

    public String descriptor() {
        StringBuilder sb = new StringBuilder();

        for (ASTParameter param : nodes)
            sb.append(param.descriptor());

        return "(" + sb.toString().trim() + ")";
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
