package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ASTMethods extends ASTNode implements Iterable<ASTMethod> {
    private Map<String, ASTMethod> methods;

    public ASTMethods(int line) {
	super(line);

	methods = new HashMap<>();
    }

    public List<ASTMethod> methods() {
	return new ArrayList<>(methods.values());
    }

    public void put(String name, ASTMethod method) {
	methods.put(name, method);
    }

    public ASTMethod get(String name) {
	return methods.get(name);
    }

    public int size() {
	return methods.size();
    }

    @Override
    public Iterator<ASTMethod> iterator() {
	return methods.values().iterator();
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	if (methods.size() == 0)
	    return String.format("%s[No Fields]", ASTNode.indent());
	
	for (ASTMethod method : methods.values())
	    sj.add(String.format("%s", method));

	return sj.toString();
    }
}
