package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.fs.FSSourceFile;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;

public class CmdParse extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	System.out.println("Parse");

	if (args.length < 1) {
	    System.out.println("Error: No file specified.");

	    return true;
	}

	FSSourceFile sf = console.parse(args[0]);

	if (sf == null)
	    return true;

	ASTObject astObject = sf.astObject();

	astObject.accept(new PrintVisitor());

	return true;
    }

    @Override
    public String toString() {
	return "Parse <source file>";
    }
}
