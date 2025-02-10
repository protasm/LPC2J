package io.github.protasm.lpc2j.parser.ast;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

import static org.objectweb.asm.Opcodes.*;

public class ASTArguments extends ASTListNode<ASTArgument> {
    public ASTArguments(int line) {
	super(line);
    }

    @Override
    public void accept(MethodVisitor mv) {
	mv.visitLdcInsn(nodes.size()); // Push array length

	mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // Create Object[]

	for (int i = 0; i < nodes.size(); i++) {
	    mv.visitInsn(DUP); // Duplicate array reference

	    mv.visitLdcInsn(i); // Push index

	    nodes.get(i).accept(mv); // Push argument value

	    // If argument is a primitive, box it (e.g., int -> Integer)
//            boxIfPrimitive(mv, arguments.get(i));

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
