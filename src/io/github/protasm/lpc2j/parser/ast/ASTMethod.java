package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTMethod extends ASTNode {
    private final String ownerName;
    private final LPCType lpcReturnType;
    private final String name;
    private final ASTParameters parameters;
    private final ASTStmtBlock body;

    public ASTMethod(int line, String ownerName, Token<LPCType> typeToken, Token<String> nameToken,
	    ASTParameters parameters, ASTStmtBlock body) {
	super(line);

	this.ownerName = ownerName;
	this.lpcReturnType = typeToken.literal();
	this.name = nameToken.lexeme();
	this.parameters = parameters;
	this.body = body;
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

    public ASTStmtBlock body() {
	return body;
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

	sb.append(String.format("%s(returnType=%s, name=%s)\n", className(), lpcReturnType, name));

	sb.append(parameters);

	sb.append(body);

	return sb.toString();
    }
}
