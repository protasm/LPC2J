package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.List;

import scanner.Token;

public class ASTParameters extends ASTNode {
    private final List<ASTParameter> parameters;

    public ASTParameters(Token startToken) {
	super(startToken);

	this.parameters = new ArrayList<>();
    }

    public void add(ASTParameter parameter) {
	parameters.add(parameter);
    }

    public String descriptor() {
	StringBuilder sb = new StringBuilder();

	for (ASTParameter param : parameters) {
	    sb.append(param.type().descriptor());
	}

	return "(" + sb.toString().trim() + ")";
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	for (ASTParameter param : parameters) {
	    sb.append("  ").append(param).append("\n");
	}

	return sb.toString().trim();
    }
}
