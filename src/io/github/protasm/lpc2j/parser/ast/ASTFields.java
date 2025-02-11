package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.parser.ast.visitor.BytecodeVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTFields extends ASTMapNode<ASTField> {
    public ASTFields(int line) {
	super(line);
    }

    @Override
    public void accept(BytecodeVisitor visitor) {
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
