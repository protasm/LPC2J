package io.github.protasm.lpc2j.parser.ast.stmt;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTStmtReturn extends ASTStatement {
    private final ASTExpression value; // return value, if any

    public ASTStmtReturn(int line, ASTExpression value) {
	super(line);

	this.value = value;
    }

    public ASTExpression value() {
	return value;
    }

    @Override
    public void accept(MethodVisitor mv) {
	if (value == null) {
	    mv.visitInsn(Opcodes.RETURN);

	    return;
	}

	value.accept(mv);

	switch (value.lpcType()) {
	case LPCINT:
	    mv.visitInsn(Opcodes.IRETURN);
	break;
	case LPCMIXED:
	case LPCSTRING:
	case LPCOBJECT:
	    mv.visitInsn(Opcodes.ARETURN);
	break;
	default:
	    throw new UnsupportedOperationException("Unsupported return value type: " + value.lpcType());
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
