package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.fs.FSSourceFile;
import io.github.protasm.lpc2j.scanner.Tokens;

public class CmdScan extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	System.out.println("Scan");

	if (args.length < 1) {
	    System.out.println("Error: No file specified.");

	    return true;
	}

	String vPathStr = pathStrOfArg(console.vPath(), args[0]);
	FSSourceFile sf = console.scan(vPathStr);

	if (sf == null)
	    return true;

	Tokens tokens = sf.tokens();

	if (tokens != null)
	    System.out.println(tokens);

	return true;
    }

    @Override
    public String toString() {
	return "Scan <source file>";
    }
}
