package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTFields extends ASTNode {
    private Map<String, ASTField> fields;
    
    public ASTFields(int line) {
	super(line);

	fields = new HashMap<>();
    }
    
    public List<ASTField> fields() {
	return new ArrayList<>(fields.values());
    }
    
    public void addField(String name, ASTField field) {
	fields.put(name, field);
    }
    
    public ASTField getField(String name) {
	return fields.get(name);
    }
    
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	
	for (ASTField field : fields.values())
	    sb.append(field);
	
	return sb.toString();
    }
}
