package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTMethod extends ASTNode {
    private final String ownerName;
    private final Symbol symbol;
    private ASTParameters parameters;
    private ASTStmtBlock body;

    public ASTMethod(int line, String ownerName, Symbol symbol) {
	super(line);

	this.ownerName = ownerName;
	this.symbol = symbol;

	parameters = null;
	body = null;
    }

    public String ownerName() {
	return ownerName;
    }

    public Symbol symbol() {
	return symbol;
    }

    public ASTParameters parameters() {
	return parameters;
    }

    public void setParameters(ASTParameters parameters) {
	this.parameters = parameters;
    }

    public ASTStmtBlock body() {
	return body;
    }

    public void setBody(ASTStmtBlock body) {
	this.body = body;
    }

    public String descriptor() {
	return parameters.descriptor() + symbol.descriptor();
    }

    @Override
    public void accept(MethodVisitor mv) {
	body.accept(mv);
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
