package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.MethodVisitor;

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
	for (ASTArgument arg : arguments)
	    arg.toBytecode(mv);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();

	for (ASTArgument arg : arguments)
	    sb.append(arg);

	return sb.toString();
    }
}
