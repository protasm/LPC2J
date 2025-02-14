package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprFieldAccess extends ASTExpression {
    private final ASTField field;

    public ASTExprFieldAccess(int line, ASTField field) {
	super(line);

	this.field = field;
    }

    public ASTField field() {
	return field;
    }

    @Override
    public LPCType lpcType() {
	return field.symbol().lpcType();
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
