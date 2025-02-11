package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.JType;
import io.github.protasm.lpc2j.parser.type.LPCType;

import static org.objectweb.asm.Opcodes.*;

public class ASTArguments extends ASTListNode<ASTArgument> {
    public ASTArguments(int line) {
	super(line);
    }

    public void paramTypes(MethodVisitor mv) {
	mv.visitLdcInsn(nodes.size());
	mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

	for (int i = 0; i < nodes.size(); i++) {
	    mv.visitInsn(DUP); // Duplicate array reference.
	    mv.visitLdcInsn(i); // Push array index.

	    // Get the LPC type for the i-th argument.
	    ASTExpression expr = nodes.get(i).expression();
	    JType jType = expr.lpcType().jType();

	    switch (jType) {
	    case JINT:
		mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
	    break;
	    case JFLOAT:
		mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
	    break;
	    case JBOOLEAN:
		mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
	    break;
	    case JSTRING:
		mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
	    break;
	    default:
		// For LPCMIXED or other types, default to Object.
		mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
	    break;
	    }

	    mv.visitInsn(AASTORE);
	}

    }

    @Override
    public void accept(MethodVisitor mv) {
	mv.visitLdcInsn(nodes.size()); // Push array length

	mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // Create Object[]

	for (int i = 0; i < nodes.size(); i++) {
	    mv.visitInsn(DUP); // Duplicate array reference

	    mv.visitLdcInsn(i); // Push index

	    nodes.get(i).accept(mv); // Push argument value

	    mv.visitInsn(AASTORE); // Store argument into array
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
