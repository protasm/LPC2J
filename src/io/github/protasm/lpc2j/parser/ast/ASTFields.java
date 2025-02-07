package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ASTFields extends ASTNode implements Iterable<ASTField> {
	private Map<String, ASTField> fields;

	public ASTFields(int line) {
		super(line);

		fields = new HashMap<>();
	}

	public List<ASTField> fields() {
		return new ArrayList<>(fields.values());
	}

	public void put(String name, ASTField field) {
		fields.put(name, field);
	}

	public ASTField get(String name) {
		return fields.get(name);
	}
	
	@Override
    public Iterator<ASTField> iterator() {
        return fields.values().iterator();
    }
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (ASTField field : fields.values())
			sb.append(field);

		return sb.toString();
	}
}
