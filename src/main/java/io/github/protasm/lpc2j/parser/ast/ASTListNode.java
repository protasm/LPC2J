package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract non-sealed class ASTListNode<T> extends ASTNode implements Iterable<T> {
    protected List<T> nodes;

    public ASTListNode(int line) {
        super(line);

        nodes = new ArrayList<>();
    }

    public ASTListNode(int line, List<T> nodes) {
        super(line);

        this.nodes = nodes;
    }

    public List<T> nodes() {
        return nodes;
    }

    public void add(T node) {
        nodes.add(node);
    }

    public T get(int i) {
        return nodes.get(i);
    }

    public int size() {
        return nodes.size();
    }

    @Override
    public Iterator<T> iterator() {
        return nodes.iterator();
    }
}
