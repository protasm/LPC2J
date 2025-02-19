package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

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
	    Path vPath = pathStrOfArg(console, args[0]);
	    String contents = basePath.contentsOf(vPath.toString());

	    if (contents == null) {
		System.out.println("Invalid file: " + args[0]);

		return true;
	    }

	    System.out.println(contents);

	    return true;
	} catch (InvalidPathException e) {
	    System.out.println(e);
	}

	return true;
    }

    @Override
    public String toString() {
	return "Display contents of a source file";
    }
}
