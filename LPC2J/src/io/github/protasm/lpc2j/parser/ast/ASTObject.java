package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTObject extends ASTNode {
    private final String parentName;
    private final String name;
    private final Map<String, ASTField> fields;
    private final List<ASTMethod> methods;

    public ASTObject(int line, String parentName, String name) {
	super(line);

	this.parentName = parentName;
	this.name = name;

	this.fields = new HashMap<>();
	this.methods = new ArrayList<>();
    }

    public String parentName() {
	return parentName;
    }

    public String name() {
	return name;
    }

    public Map<String, ASTField> fields() {
	return fields;
    }

    public List<ASTMethod> methods() {
	return methods;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	if (parentName != null)
	    sb.append(String.format("%s(parentName=%s, name=%s)\n", className(), parentName, name));
	else
	    sb.append(String.format("%s(name=%s)\n", className(), name));
	
	for (ASTField field : fields.values())
	    sb.append(field);

	sb.append("\n");

	for (ASTMethod method : methods)
	    sb.append(method).append("\n");
	
	return sb.toString();
    }
}
