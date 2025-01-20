package io.github.protasm.lpc2j.parser.ast.stmt;

import ast.expr.ASTExprCall;
import scanner.Token;

public class ASTStmtCall extends ASTStatement {
    private final ASTExprCall call;

    public ASTStmtCall(Token startToken, ASTExprCall call) {
	super(startToken);

	this.call = call;
    }

    public ASTExprCall call() {
	return call;
    }

    @Override
    public String toString() {
	return String.format("ASTStmtCall(call=%s)", call);
    }
}
