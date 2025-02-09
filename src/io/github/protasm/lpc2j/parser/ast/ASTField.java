package io.github.protasm.lpc2j.parser.ast;

import java.util.StringJoiner;

import io.github.protasm.lpc2j.parser.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

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
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	sj.add(String.format("%s%s(%s)", ASTNode.indent(), className(), symbol));

	if (initializer != null) {
	    ASTNode.indentLvl++;

	    sj.add(String.format("%s", initializer));

	    ASTNode.indentLvl--;
	}

	return sj.toString();
    }
}
