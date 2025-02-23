package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.InvalidPathException;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.fs.FSBasePath;

public class CmdFileCat extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	FSBasePath basePath = console.basePath();

	if (args.length == 0) {
	    System.out.println("Usage: cat [<file.lpc> | <file.c>]");

	    return true;
	}

	try {
	    String vPathStr = pathStrOfArg(console.vPath(), args[0]);
	    String contents = basePath.contentsOf(vPathStr);

	    if (contents == null) {
		System.out.println("Invalid file: " + args[0]);

		return true;
	    }

	    System.out.println(contents);
	} catch (InvalidPathException e) {
	    System.out.println("Error displaying contents of file: " + args[0]);
	}

	return true;
    }

    @Override
    public String toString() {
	return "Display contents of a source file";
    }
}
