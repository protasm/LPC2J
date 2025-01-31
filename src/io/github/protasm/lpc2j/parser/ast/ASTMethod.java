package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.LPCType;

public class ASTMethod extends ASTNode {
	private final String ownerName;
	private final LPCType lpcReturnType;
	private final String name;
	private ASTParameters parameters;
	private ASTStmtBlock body;

	public ASTMethod(int line, String ownerName, LPCType lpcType, String name) {
		super(line);

		this.ownerName = ownerName;
		this.lpcReturnType = lpcType;
		this.name = name;

		parameters = null;
		body = null;
	}

	public String ownerName() {
		return ownerName;
	}

	public LPCType lpcReturnType() {
		return lpcReturnType;
	}

	public String name() {
		return name;
	}

	public ASTParameters parameters() {
		return parameters;
	}

	public void setParameters(ASTParameters parameters) {
		this.parameters = parameters;
	}

	public ASTStmtBlock body() {
		return body;
	}

	public void setBody(ASTStmtBlock body) {
		this.body = body;
	}

	public String descriptor() {
		return parameters.descriptor() + lpcReturnType.jType().descriptor();
	}

	@Override
	public void toBytecode(MethodVisitor mv) {
		body.toBytecode(mv);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("%s(%s %s)\n", className(), lpcReturnType, name));

		sb.append(parameters);

		sb.append(body);

		return sb.toString();
	}
}
