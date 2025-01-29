package io.github.protasm.lpc2j.parser;

import java.util.ListIterator;
import java.util.Stack;

public class Locals {
    private Stack<Local> locals;
    private int workingScopeDepth;

    public Locals() {
	locals = new Stack<>();
	workingScopeDepth = 1;

	// Locals slot 0 reserved for "this"
	Local local = new Local(null, "this");

	local.setScopeDepth(0);

	locals.push(local);
    }

    public Stack<Local> locals() {
	return locals;
    }

    public boolean hasCollision(String name) {
	Local local = get(name);

	if (local != null)
	    if (local.scopeDepth() == workingScopeDepth)
		return true;

	return false;
    }

    public Local get(String name) {
	ListIterator<Local> localsItr = locals.listIterator(locals.size());

	while (localsItr.hasPrevious()) {
	    Local local = localsItr.previous();

	    if (local.name().equals(name))
		if (local.scopeDepth() == -1) // "sentinel" value
		    throw new ParseException("Can't read local variable in its own initializer.");
		else
		    return local;
	}

	return null;
    }

    public void add(Local local, boolean markInitialized) {
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
