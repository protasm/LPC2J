package io.github.protasm.lpc2j.parser.ast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class ASTMapNode<T> extends ASTNode implements Iterable<T> {
	protected Map<String, T> nodes;

	public ASTMapNode(int line) {
		super(line);

		nodes = new HashMap<>();
	}

	public ASTMapNode(int line, Map<String, T> nodes) {
		super(line);

		this.nodes = nodes;
	}

	public Map<String, T> nodes() {
		return nodes;
	}

	public void put(String name, T node) {
		nodes.put(name, node);
	}

	public T get(String name) {
		return nodes.get(name);
	}

	public int size() {
		return nodes.size();
	}

	@Override
	public Iterator<T> iterator() {
		return nodes.values().iterator();
	}
}
