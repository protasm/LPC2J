package io.github.protasm.lpc2j.parser.ast.stmt;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class ASTStmtReturn extends ASTStatement {
    private final ASTExpression expression; // return value, if any

    public ASTStmtReturn(int line, ASTExpression expression) {
	super(line);

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

	switch (expression.lpcType()) {
	case LPCINT:
	    mv.visitInsn(Opcodes.IRETURN);
	    break;
	case LPCSTRING:
	case LPCOBJECT:
	    mv.visitInsn(Opcodes.ARETURN);
	    break;
	default:
	    throw new UnsupportedOperationException("Unsupported return value type: " + expression.lpcType());
	}
    }

    @Override
    public String toString() {
	return String.format("%s(expression=%s)", className, expression);
    }
}
