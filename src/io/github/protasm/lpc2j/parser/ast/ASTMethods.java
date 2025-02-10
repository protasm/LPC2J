package io.github.protasm.lpc2j.parser.ast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTMethods extends ASTNode implements Iterable<ASTMethod> {
	private Map<String, ASTMethod> methods;

	public ASTMethods(int line) {
		super(line);

		methods = new HashMap<>();
	}

	public void put(String name, ASTMethod method) {
		methods.put(name, method);
	}

	public ASTMethod get(String name) {
		return methods.get(name);
	}

	public int size() {
		return methods.size();
	}

	@Override
	public Iterator<ASTMethod> iterator() {
		return methods.values().iterator();
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
