package io.github.protasm.lpc2j.parser.ast.stmt;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCall;

public class ASTStmtCall extends ASTStatement {
    private final ASTExprCall call;

    public ASTStmtCall(int line, ASTExprCall call) {
	super(line);

	this.call = call;
    }

    public ASTExprCall call() {
	return call;
    }

    @Override
    public String toString() {
	return String.format("%s(call=%s)", className, call);
    }
}
