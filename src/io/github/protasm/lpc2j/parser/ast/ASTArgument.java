package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTArgument extends ASTNode {
    private final ASTExpression expression;

    public ASTArgument(int line, ASTExpression expr) {
	super(line);

	this.expression = expr;
    }

    public ASTExpression expression() {
	return expression;
    }

    @Override
    public void accept(MethodVisitor mv) {
	expression.accept(mv);

	LPCType type = expression.lpcType();

	if (type == null) // Without type information, assume no boxing is needed.
	    return;

	// box primitive value, if needed
	switch (type.jType()) {
	case JINT: // Integer.valueOf(int)
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf",
		    "(I)Ljava/lang/Integer;", false);
	    break;
	case JFLOAT: // Float.valueOf(float)
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf",
		    "(F)Ljava/lang/Float;", false);
	    break;
	case JBOOLEAN: // Boolean.valueOf(boolean)
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf",
		    "(Z)Ljava/lang/Boolean;", false);
	    break;
	default: // For non-primitive types (or types that don't need boxing).
	    break;
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
