package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class ASTExprLocalAccess extends ASTExpression {
    private final ASTLocal local;

    public ASTExprLocalAccess(int line, ASTLocal local) {
	super(line);

	this.local = local;
    }

    public ASTLocal local() {
	return local;
    }

    @Override
    public LPCType lpcType() {
	return local.symbol().lpcType();
    }

    @Override
    public void accept(MethodVisitor mv) {
	switch (local.symbol().lpcType()) {
	case LPCINT:
	case LPCSTATUS:
	    mv.visitVarInsn(ILOAD, local.slot());
	    break;
	case LPCSTRING:
	case LPCOBJECT:
	    mv.visitVarInsn(ALOAD, local.slot());
	    break;
	default:
	    throw new IllegalStateException("Unsupported type: " + local.symbol().lpcType());
	}
    }

    @Override
    public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
	visitor.visit(this, lpcType);
    }

    @Override
    public void accept(PrintVisitor visitor) {
	visitor.visit(this);
    }
}
