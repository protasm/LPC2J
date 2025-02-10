package io.github.protasm.lpc2j.parser;

import java.util.ListIterator;
import java.util.Stack;

import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class Locals {
    private Stack<ASTLocal> locals;
    private int workingScopeDepth;

    public Locals() {
	locals = new Stack<>();
	workingScopeDepth = 1;

	// Locals slot 0 reserved for "this"
	ASTLocal local = new ASTLocal(0, new Symbol(LPCType.LPCOBJECT, "this"));

	local.setScopeDepth(0);

	locals.push(local);
    }

    public Stack<ASTLocal> locals() {
	return locals;
    }

    public boolean hasCollision(String name) {
	ASTLocal local = get(name);

	if (local != null)
	    if (local.scopeDepth() == workingScopeDepth)
		return true;

	return false;
    }

    public ASTLocal get(String name) {
	ListIterator<ASTLocal> localsItr = locals.listIterator(locals.size());

	while (localsItr.hasPrevious()) {
	    ASTLocal local = localsItr.previous();

	    if (local.symbol().name().equals(name))
		if (local.scopeDepth() == -1) // "sentinel" value
		    throw new ParseException("Can't read local variable in its own initializer.");
		else
		    return local;
	}

	return null;
    }

    public void add(ASTLocal local, boolean markInitialized) {
	locals.push(local);

	local.setSlot(locals.size() - 1);

	if (markInitialized)
	    local.setScopeDepth(workingScopeDepth);
    }

    public void beginScope() {
	workingScopeDepth += 1;
    }

    public void endScope() {
	workingScopeDepth -= 1;

	// pop all locals belonging to the expiring scope
	while (!(locals.isEmpty()) && (locals().peek().scopeDepth() > workingScopeDepth))
	    locals.pop();
    }
}
