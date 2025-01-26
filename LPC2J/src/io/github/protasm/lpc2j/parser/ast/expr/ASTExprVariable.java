package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTExprVariable extends ASTExpression {
    private final String name;

    public ASTExprVariable(int line, Token<String> nameToken) {
	super(line);

	this.name = nameToken.lexeme();
    }

    public String name() {
	return name;
    }

    @Override
    public LPCType lpcType() {
	return LPCType.LPCNULL; // TODO
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	//TODO
    }

    @Override
    public String dump(int level) {
	StringBuilder sb = new StringBuilder();
	
	sb.append(" ".repeat(level * 2));
	
	sb.append(String.format("%s(name=%s)", className(), name));
	
	return sb.toString();
    }
}
