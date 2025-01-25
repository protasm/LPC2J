package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.List;

public class ASTObject {
    private final String parent;
    private final String name;
    private final List<ASTField> fields;
    private final List<ASTMethod> methods;

    public ASTObject(String parent, String name) {
	this.parent = parent;
	this.name = name;

	this.fields = new ArrayList<>();
	this.methods = new ArrayList<>();
    }

    public String parent() {
	return parent;
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

	sb.append("ASTObject(name=").append(name);

	if (parent != null) {
	    sb.append(", parent=").append(parent);
	}

	sb.append(")\n\nFields:\n");

	for (ASTField field : fields) {
	    sb.append("  ").append(field).append("\n");
	}

	sb.append("\nMethods:\n");

	for (ASTMethod method : methods) {
	    sb.append(method).append("\n\n");
	}

	return sb.toString().trim();
    }
}
