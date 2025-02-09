package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ASTParameters extends ASTNode {
    private final List<ASTParameter> parameters;

    public ASTParameters(int line) {
	super(line);

	this.parameters = new ArrayList<>();
    }

    public void add(ASTParameter parameter) {
	parameters.add(parameter);
    }
    
    public int size() {
	return parameters.size();
    }

    public String descriptor() {
	StringBuilder sb = new StringBuilder();

	for (ASTParameter param : parameters)
	    sb.append(param.descriptor());

	return "(" + sb.toString().trim() + ")";
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	if (parameters.size() == 0)
	    return String.format("%s[No Parameters]", ASTNode.indent());

	for (ASTParameter param : parameters)
	    sj.add(String.format("%s", param));

	return sj.toString();
    }
}
