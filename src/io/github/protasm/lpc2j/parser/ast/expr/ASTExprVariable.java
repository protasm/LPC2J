package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

public class ASTExprVariable extends ASTExpression {
    private final ASTNode value;
//
    public ASTExprVariable(int line, ASTNode value) {
	super(line);

	this.value = value;
    }
//
//    public ASTNode value() {
//	return value;
//    }
//
    @Override
    public LPCType lpcType() {
	if (value instanceof ASTField)
	    return ((ASTField) value).lpcType();
	
	return null;
    }
//
//    @Override
//    public void toBytecode(MethodVisitor mv) {
//	if (value instanceof ASTField)
//	    mv.visitInsn(Opcodes.GETFIELD);
//    }
//
    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	sb.append(String.format("%s(value=%s)", className(), value));

	return sb.toString();
    }
}
