package io.github.protasm.lpc2j.parser.ast;

import java.util.List;

public class ASTObject extends ASTNode {
    private String parentName;
    private final String name;
    private final ASTFields fields;
    private final ASTMethods methods;

    public ASTObject(int line, String name) {
	super(line);

	this.name = name;

	parentName = null;
	fields = new ASTFields(line);
	methods = new ASTMethods(line);
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

    public List<ASTField> fields() {
	return fields.fields();
    }
    
    public void addField(ASTField field) {
	fields.addField(field.name(), field);
    }
    
    public ASTField getField(String name) {
	return fields.getField(name);
    }

    public List<ASTMethod> methods() {
	return methods.methods();
    }
    
    public ASTMethod getMethod(String name) {
	return methods.getMethod(name);
    }
    
    public void addMethod(ASTMethod method) {
	methods.addMethod(method.name(), method);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	if (parentName != null)
	    sb.append(String.format("%s(%s inherits %s)\n", className(), name, parentName));
	else
	    sb.append(String.format("%s(%s)\n", className(), name));

	sb.append(fields);

	sb.append(methods);

	return sb.toString();
    }
}
