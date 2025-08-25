package io.github.protasm.lpc2j.parser.ast;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

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
	public void accept(Compiler visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
		visitor.visit(this, lpcType);
	}

	@Override
	public void accept(PrintVisitor visitor) {
		visitor.visit(this);
	}
}
