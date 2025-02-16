package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;

public class CmdCompile extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	System.out.println("Compile");

	if (args.length < 1) {
	    System.out.println("Error: No file specified.");

	    return true;
	}

	console.compile(args[0]);

	return true;
    }

    @Override
    public String toString() {
	return "Compile <source file>";
    }
}
