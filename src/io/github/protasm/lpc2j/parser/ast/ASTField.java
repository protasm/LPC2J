package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTField extends ASTNode {
    private final String ownerName;
    private final Symbol symbol;
    private ASTExpression initializer;

    public ASTField(int line, String ownerName, Symbol symbol) {
	super(line);

	this.ownerName = ownerName;
	this.symbol = symbol;

	initializer = null;
    }

    public String ownerName() {
	return ownerName;
    }

    public Symbol symbol() {
	return symbol;
    }

    public ASTExpression initializer() {
	return initializer;
    }

    public void setInitializer(ASTExpression expr) {
	this.initializer = expr;
    }

    public String descriptor() {
	return symbol.descriptor();
    }

    @Override
    public void accept(MethodVisitor visitor) {
	// TODO Auto-generated method stub

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
