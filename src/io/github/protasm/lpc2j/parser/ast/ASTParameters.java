package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class ASTParameters extends ASTNode {
    private final List<ASTParameter> parameters;

    public ASTParameters(int line) {
	super(line);

	this.parameters = new ArrayList<>();
    }

    public void add(ASTParameter parameter) {
	parameters.add(parameter);
    }

    public String descriptor() {
	StringBuilder sb = new StringBuilder();

	for (ASTParameter param : parameters)
	    sb.append(param.descriptor());

	return "(" + sb.toString().trim() + ")";
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	for (ASTParameter param : parameters)
	    sb.append(param);

	return sb.toString();
    }
}
