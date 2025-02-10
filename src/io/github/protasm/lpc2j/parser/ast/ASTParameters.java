package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTParameters extends ASTNode implements Iterable<ASTParameter> {
	private final List<ASTParameter> parameters;

	public ASTParameters(int line) {
		super(line);

		this.parameters = new ArrayList<>();
	}

	public void add(ASTParameter parameter) {
		parameters.add(parameter);
	}

	public int size() {
		return parameters.size();
	}

	public String descriptor() {
		StringBuilder sb = new StringBuilder();

		for (ASTParameter param : parameters)
			sb.append(param.descriptor());

		return "(" + sb.toString().trim() + ")";
	}

	@Override
	public Iterator<ASTParameter> iterator() {
		return parameters.iterator();
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
