package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract non-sealed class ASTMapNode<T> extends ASTNode implements Iterable<T> {
    // Preserve duplicate declarations by keeping every node keyed and in insertion order.
    protected Map<String, List<T>> nodes;
    private final List<T> orderedNodes;

    public ASTMapNode(int line) {
        super(line);

        nodes = new LinkedHashMap<>();
        orderedNodes = new ArrayList<>();
    }

    public ASTMapNode(int line, Map<String, List<T>> nodes) {
        super(line);

        this.nodes = (nodes != null) ? new LinkedHashMap<>(nodes) : new LinkedHashMap<>();
        orderedNodes = new ArrayList<>();
        if (nodes != null)
            nodes.values().forEach(orderedNodes::addAll);
    }

    public Map<String, List<T>> nodes() {
        return nodes;
    }

    public List<T> all() {
        return Collections.unmodifiableList(orderedNodes);
    }

    public void put(String name, T node) {
        nodes.computeIfAbsent(name, key -> new ArrayList<>()).add(node);
        orderedNodes.add(node);
    }

    public T get(String name) {
        List<T> entries = nodes.get(name);
        if (entries == null || entries.isEmpty())
            return null;

        return entries.get(entries.size() - 1);
    }

    public T get(String name, int occurrence) {
        List<T> entries = nodes.get(name);

        if (entries == null || occurrence < 0 || occurrence >= entries.size())
            return null;

        return entries.get(occurrence);
    }

    public List<T> getAll(String name) {
        List<T> entries = nodes.get(name);

        if (entries == null)
            return List.of();

        return Collections.unmodifiableList(entries);
    }

    public int size() {
        return orderedNodes.size();
    }

    @Override
    public Iterator<T> iterator() {
        return orderedNodes.iterator();
    }
}
