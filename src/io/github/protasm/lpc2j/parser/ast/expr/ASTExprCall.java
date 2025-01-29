package io.github.protasm.lpc2j.parser.ast.expr;

import java.util.List;

import io.github.protasm.lpc2j.LPCType;

public class ASTExprCall extends ASTExpression {
	private final String methodName;
	private final List<ASTExpression> arguments; // TODO: replace with new class ASTArgList

	public ASTExprCall(int line, String methodName, List<ASTExpression> arguments) {
		super(line);

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
	public LPCType lpcType() {
		// TODO Auto-generated method stub
		return null; // temp
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s(methodName=%s", className(), methodName));
		sb.append(", arguments=[");

		for (ASTExpression arg : arguments)
			sb.append(arg).append(", ");

		if (!arguments.isEmpty())
			sb.setLength(sb.length() - 2); // Remove trailing comma and space

		sb.append("])");

		return sb.toString();
	}
}
