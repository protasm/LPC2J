package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.Symbol;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;

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
    public void toBytecode(MethodVisitor mv) {
	body.toBytecode(mv);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(%s)\n", className(), symbol));

	sb.append(parameters);

	sb.append(body);

	return sb.toString();
    }
}
