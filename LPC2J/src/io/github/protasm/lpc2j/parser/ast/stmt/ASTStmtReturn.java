package io.github.protasm.lpc2j.parser.ast.stmt;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ast.expr.ASTExpression;
import scanner.Token;

public class ASTStmtReturn extends ASTStatement {
    private final ASTExpression expression; // return value, if any

    public ASTStmtReturn(Token startToken, ASTExpression expression) {
	super(startToken);

	this.expression = expression;
    }

    public ASTExpression expression() {
	return expression;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	if (expression == null) {
	    mv.visitInsn(Opcodes.RETURN);

	    return;
	}

	expression.toBytecode(mv);

	switch (expression.type()) {
	case LT_INT:
	    mv.visitInsn(Opcodes.IRETURN);
	    break;
	case LT_STRING:
	case LT_OBJECT:
	    mv.visitInsn(Opcodes.ARETURN);
	    break;
	default:
	    throw new UnsupportedOperationException("Unsupported return value type: " + expression.type());
	}
    }

    @Override
    public String toString() {
	return String.format("ASTStmtReturn(expression=%s)", expression);
    }
}
