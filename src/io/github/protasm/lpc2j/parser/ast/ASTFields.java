package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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

    public int size() {
	return fields.size();
    }

    @Override
    public Iterator<ASTField> iterator() {
	return fields.values().iterator();
    }

    @Override
    public String toString() {
	if (fields.size() == 0)
	    return String.format("%s[No Fields]", ASTNode.indent());
	    
	StringJoiner sj = new StringJoiner("\n");
	
	sj.add(String.format("%s[FIELDS]", ASTNode.indent()));
	
	ASTNode.indentLvl++;

	for (ASTField field : fields.values())
	    sj.add(String.format("%s", field));

	ASTNode.indentLvl--;

	return sj.toString();
    }
}
