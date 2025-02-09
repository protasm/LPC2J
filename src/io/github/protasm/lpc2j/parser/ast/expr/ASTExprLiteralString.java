package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTNode;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTExprLiteralString extends ASTExpression {
    private final String value;

    public ASTExprLiteralString(int line, Token<String> token) {
	super(line);

	this.value = token.literal();
    }

    public String value() {
	return value;
    }

    @Override
    public LPCType lpcType() {
	return LPCType.LPCSTRING;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	mv.visitLdcInsn(value);
    }

    @Override
    public String toString() {
	return String.format("%s%s(\"%s\")", ASTNode.indent(), className(), value);
    }
}
