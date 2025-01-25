package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class ASTObject {
    private final String parentName;
    private final String name;
    private final List<ASTField> fields;
    private final List<ASTMethod> methods;

    public ASTObject(String parentName, String name) {
	this.parentName = parentName;
	this.name = name;

	this.fields = new ArrayList<>();
	this.methods = new ArrayList<>();
    }

    public String parentName() {
	return parentName;
    }

    public String name() {
	return name;
    }

    public List<ASTField> fields() {
	return fields;
    }

    public List<ASTMethod> methods() {
	return methods;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(getClass().getSimpleName());
	sb.append("(name=").append(name);

	if (parentName != null)
	    sb.append(", parent=").append(parentName);

	sb.append(")\n\nFields:\n");

	for (ASTField field : fields)
	    sb.append("  ").append(field).append("\n");

	sb.append("\nMethods:\n");

	for (ASTMethod method : methods)
	    sb.append(method).append("\n\n");

	return sb.toString().trim();
    }
}
