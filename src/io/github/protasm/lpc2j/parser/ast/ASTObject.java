package io.github.protasm.lpc2j.parser.ast;

import java.util.HashMap;
import java.util.Map;

public class ASTObject extends ASTNode {
	private String parentName;
	private final String name;
	private final Map<String, ASTField> fields;
	private final Map<String, ASTMethod> methods;

	public ASTObject(int line, String name) {
		super(line);

		this.name = name;

		parentName = null;
		fields = new HashMap<>();
		methods = new HashMap<>();
	}

	public String parentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String name() {
		return name;
	}

	public Map<String, ASTField> fields() {
		return fields;
	}

	public void addField(ASTField field) {
		fields.put(field.name(), field);
	}

	public Map<String, ASTMethod> methods() {
		return methods;
	}

	public void addMethod(ASTMethod method) {
		methods.put(method.name(), method);
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

		for (ASTMethod method : methods.values())
			sb.append(method).append("\n");

		return sb.toString();
	}
}
