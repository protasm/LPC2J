package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTNode;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTExprVariable extends ASTExpression {
    private final ASTNode node;

    public ASTExprVariable(int line, ASTNode node) {
	super(line);

	this.node = node;
    }

    public ASTNode node()) {
	return node;
    }

    @Override
    public LPCType lpcType() {
	if (node instanceof ASTField)
	    return ((ASTField) node).lpcType();
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	if (node instanceof ASTField)
	    mv.visitInsn(Opcodes.GETFIELD);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	
	sb.append(String.format("%s(name=%s)", className(), name));
	
	return sb.toString();
    }
}
