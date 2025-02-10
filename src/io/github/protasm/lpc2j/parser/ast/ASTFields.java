package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

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
	public void accept(MethodVisitor visitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(PrintVisitor visitor) {
		visitor.visit(this);
	}
}
