package io.github.protasm.lpc2j;

public class Local {
    private Variable jVar;
    private int scopeDepth;
    private boolean isCaptured;

    public Local(Variable jVar) {
	this.jVar = jVar;
	this.scopeDepth = -1;

	isCaptured = false;
    }

    public Variable jVar() {
	return jVar;
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
	return "[ " + jVar.name() + " (" + scopeDepth + ") ]";
    }
}
