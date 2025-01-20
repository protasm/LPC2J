package io.github.protasm.lpc2j.parser.ast.expr;

import java.util.List;

import scanner.Token;

public class ASTExprCall extends ASTExpression {
    private final String methodName;
    private final List<ASTExpression> arguments;

    public ASTExprCall(Token startToken, String methodName, List<ASTExpression> arguments) {
	super(startToken);

	this.methodName = methodName;
	this.arguments = arguments;
    }

    public String methodName() {
	return methodName;
    }

    public List<ASTExpression> arguments() {
	return arguments;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("ASTExprCall(methodName=%s", methodName));
	sb.append(", arguments=[");

	for (ASTExpression arg : arguments) {
	    sb.append(arg).append(", ");
	}

	if (!arguments.isEmpty()) {
	    sb.setLength(sb.length() - 2); // Remove trailing comma and space
	}

	sb.append("])");

	return sb.toString();
    }
}
