package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;

public class CmdCompile extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	System.out.println("Compile");

	if (args.length < 1) {
	    System.out.println("Error: No file specified.");

	    return true;
	}

	String vPathStr = pathStrOfArg(console.vPath(), args[0]);
	FSSourceFile sf = console.compile(vPathStr);

	if (sf == null)
	    return true;

	System.out.println("Success!  Compiled to " + sf.dotName());

	return true;
    }

    @Override
    public String toString() {
	return "Compile <source file>";
    }
}
