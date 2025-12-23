package io.github.protasm.lpc2j.parser;

import java.util.ListIterator;
import java.util.Stack;

import io.github.protasm.lpc2j.parser.ast.ASTLocal;

public class Locals {
    private final Stack<ASTLocal> locals;
    private int workingScopeDepth;

    public Locals() {
        locals = new Stack<>();
        workingScopeDepth = 0;
    }

    public Stack<ASTLocal> locals() {
        return locals;
    }

    public ASTLocal get(String name) {
        ListIterator<ASTLocal> localsItr = locals.listIterator(locals.size());

        while (localsItr.hasPrevious()) {
            ASTLocal local = localsItr.previous();

            if (local.symbol().name().equals(name))
                return local;
        }

        return null;
    }

    public void add(ASTLocal local) {
        locals.push(local);

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
