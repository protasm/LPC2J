package io.github.protasm.lpc2j.parser.ast;

import java.util.StringJoiner;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
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
    public void typeInference(LPCType lpcType) {
	body.typeInference(symbol.lpcType());
    }

    @Override
    public void accept(MethodVisitor mv) {
	body.accept(mv);
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("\n%s%s(%s)", ASTNode.indent(), className(), symbol));

	ASTNode.indentLvl++;

	sj.add(String.format("%s", parameters));
	sj.add(String.format("%s", body));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
