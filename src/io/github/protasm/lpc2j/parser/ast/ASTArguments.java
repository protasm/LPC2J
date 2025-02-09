package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

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

    public ASTArgument get(int i) {
	return arguments.get(i);
    }

    public int size() {
	return arguments.size();
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
	StringJoiner sj = new StringJoiner("\n");

	if (arguments.size() == 0)
	    return String.format("%s[No Arguments]", ASTNode.indent());

	for (ASTArgument arg : arguments)
	    sj.add(String.format("%s", arg));

	return sj.toString();
    }
}
