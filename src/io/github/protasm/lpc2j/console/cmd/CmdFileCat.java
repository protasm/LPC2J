package io.github.protasm.lpc2j.console.cmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

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
	    Path argPath = Path.of(args[0]);

	    // handle relative path argument
	    if (!argPath.isAbsolute() && (console.vPath() != null))
		argPath = Paths.get(console.vPath().toString(), args[0]);

	    File file = basePath.fileAt(argPath.toString());

	    if (file == null) {
		System.out.println("Invalid file: " + args[0]);

		return true;
	    }

	    try {
		Files.lines(file.toPath()).forEach(System.out::println);
	    } catch (IOException e) {
		System.out.println("Error reading file: " + args[0]);
	    }
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
