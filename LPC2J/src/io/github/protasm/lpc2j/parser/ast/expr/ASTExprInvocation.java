package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.LPCType;

public class ASTExprInvocation extends ASTExpression {
    private final String objectName;
    private final ASTExprCall call;

    public ASTExprInvocation(int line, String objectName, ASTExprCall call) {
	super(line);

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
    public LPCType lpcType() {
	// TODO Auto-generated method stub
	return null; //TODO
    }

    @Override
    public String toString() {
	return String.format("%s(objectName=%s, call=%s)", className, objectName, call);
    }

}
