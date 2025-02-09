package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTNode;

public class ASTExprLiteralTrue extends ASTExpression {
    public ASTExprLiteralTrue(int line) {
	super(line);
    }

    @Override
    public LPCType lpcType() {
	return LPCType.LPCSTATUS;
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	mv.visitInsn(Opcodes.ICONST_1);
    }

    @Override
    public String toString() {
	return String.format("%s%s", ASTNode.indent(), className());
    }
}
