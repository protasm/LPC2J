package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;

public class CmdLoad extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	System.out.println("Load");

	if (args.length < 1) {
	    System.out.println("Error: No file specified.");

	    return false;
	}

	console.load(args[0]);

	return false;
    }
}
