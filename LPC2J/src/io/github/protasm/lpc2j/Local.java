package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.scanner.Token;

public class Local extends Variable {
    private int scopeDepth;
    private boolean isCaptured;

    public Local(JType jType, String name) {
	super(jType, name);

	scopeDepth = -1;
	isCaptured = false;
    }

    public Local(Token typeToken, Token nameToken) {
	super(typeToken, nameToken);

	scopeDepth = -1;
	isCaptured = false;
    }

    public int scopeDepth() {
	return scopeDepth;
    }

    public void setScopeDepth(int scopeDepth) {
	this.scopeDepth = scopeDepth;
    }

    public boolean isCaptured() {
	return isCaptured;
    }

    public void setIsCaptured(boolean isCaptured) {
	this.isCaptured = isCaptured;
    }

    @Override
    public String toString() {
	return "[ " + name() + " (" + scopeDepth + ") ]";
    }
}
