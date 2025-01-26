package io.github.protasm.lpc2j.parser.ast.stmt;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class ASTStmtReturn extends ASTStatement {
    private final ASTExpression expr; // return value, if any

    public ASTStmtReturn(int line, ASTExpression expr) {
	super(line);

	this.expr = expr;
    }

    public ASTExpression expression() {
	return expr;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	if (expr == null) {
	    mv.visitInsn(Opcodes.RETURN);

	    return;
	}

	expr.toBytecode(mv);

	switch (expr.lpcType()) {
	case LPCINT:
	    mv.visitInsn(Opcodes.IRETURN);
	    break;
	case LPCSTRING:
	case LPCOBJECT:
	    mv.visitInsn(Opcodes.ARETURN);
	    break;
	default:
	    throw new UnsupportedOperationException("Unsupported return value type: " + expr.lpcType());
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	
	if (expr != null)
	    sb.append(String.format("%s(expr=%s)\n", className(), expr));
	else
	    sb.append(String.format("%s()\n", className()));
	
	return sb.toString();
    }
}
