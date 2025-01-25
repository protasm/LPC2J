package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import ast.stmt.ASTStmtBlock;
import common.LTType;
import scanner.Token;

public class ASTMethod extends ASTNode {
    private final LTType returnType;
    private final String name;
    private final ASTParameters parameters;
    private final ASTStmtBlock body;

    public ASTMethod(Token startToken, Token nameToken, ASTParameters parameters, ASTStmtBlock body) {
	super(startToken);

	this.returnType = startToken.type().toLTType();
	this.name = nameToken.lexeme();
	this.parameters = parameters;
	this.body = body;
    }

    public LTType returnType() {
	return returnType;
    }

    public String name() {
	return name;
    }

    public ASTParameters parameters() {
	return parameters;
    }

    public ASTStmtBlock body() {
	return body;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	body.toBytecode(mv);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("ASTMethod(returnType=%s, name=%s)\n", returnType, name));

	sb.append("Parameters:\n");

	sb.append(parameters);

	sb.append("Body:\n");

	sb.append(body);

	return sb.toString().trim();
    }
}
