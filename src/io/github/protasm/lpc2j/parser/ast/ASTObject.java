package io.github.protasm.lpc2j.parser.ast;

import java.util.StringJoiner;

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

    public ASTFields fields() {
	return fields;
    }

    public ASTMethods methods() {
	return methods;
    }

    @Override
    public String toString() {
	StringJoiner sj = new StringJoiner("\n");

	if (parentName != null)
	    sj.add(String.format("%s(%s inherits %s)", className(), name, parentName));
	else
	    sj.add(String.format("%s(%s)", className(), name));

	ASTNode.indentLvl++;

	sj.add(String.format("%s", fields));
	sj.add(String.format("%s", methods));

	ASTNode.indentLvl--;

	sj.add("End Object\n");

	return sj.toString();
    }
}
