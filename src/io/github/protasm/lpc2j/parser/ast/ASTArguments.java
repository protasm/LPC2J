package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ASTArguments extends ASTNode {
    private final List<ASTArgument> arguments;

    public ASTArguments(int line) {
	super(line);

	this.arguments = new ArrayList<>();
    }

    public void add(ASTArgument argument) {
	arguments.add(argument);
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	mv.visitLdcInsn(arguments.size()); // Push array length

	mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // Create Object[]

	for (int i = 0; i < arguments.size(); i++) {
	    mv.visitInsn(DUP); // Duplicate array reference

	    mv.visitLdcInsn(i); // Push index

	    arguments.get(i).toBytecode(mv); // Push argument value

	    // If argument is a primitive, box it (e.g., int -> Integer)
//            boxIfPrimitive(mv, arguments.get(i));

	    mv.visitInsn(AASTORE); // Store argument into array
	}
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	for (ASTArgument arg : arguments)
	    sb.append(arg);

	return sb.toString();
    }
}
