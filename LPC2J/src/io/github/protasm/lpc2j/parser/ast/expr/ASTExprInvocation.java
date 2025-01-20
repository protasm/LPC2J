package io.github.protasm.lpc2j.parser.ast.expr;

import scanner.Token;

public class ASTExprInvocation extends ASTExpression {
    private final String objectName;
    private final ASTExprCall call;

    public ASTExprInvocation(Token startToken, String objectName, ASTExprCall call) {
	super(startToken);

	this.objectName = objectName;
	this.call = call;
    }

    public String objectName() {
	return objectName;
    }

    public ASTExprCall methodCall() {
	return call;
    }

    @Override
    public String toString() {
	return String.format("ASTExprInvocation(objectName=%s, call=%s)", objectName, call);
    }
}
