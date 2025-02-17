package io.github.protasm.lpc2j.console.cmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import io.github.protasm.lpc2j.console.Console;

public class CmdFileCat extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	if (args.length == 0) {
	    System.out.println("Usage: cat <file.lpc>");

	    return true;
	}

	try {
	    Path argPath = Path.of(args[0]);
	    Path filePath = console.basePath().resolve(argPath);

	    File file = new File(filePath.toString());

	    if (!file.exists()) {
		System.out.println("File does not exist: " + args[0]);

		return true;
	    }

	    try {
		Files.lines(file.toPath()).forEach(System.out::println);
	    } catch (IOException e) {
		System.out.println("cat: Error reading file");
	    }
	} catch (InvalidPathException e) {
	    System.out.println(e);
	}

	return true;
    }

    @Override
    public String toString() {
	return "Display contents of an .lpc file";
    }
}
