package io.github.protasm.lpc2j.console.cmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import io.github.protasm.lpc2j.console.Console;

public class CmdFileCat extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	if (args.length == 0) {
	    System.out.println("Usage: cat <file.lpc>");

	    return true;
	}

	File file = new File(
		console.vPath().baseDir(),
		new File(
			console.vPath().currVirtualDir(),
			args[0]).getPath());

	if (!file.exists() || !file.isFile() || !file.getName().endsWith(".lpc")) {
	    System.out.println("cat: Invalid file: " + args[0]);

	    return true;
	}

	try {
	    Files.lines(file.toPath()).forEach(System.out::println);
	} catch (IOException e) {
	    System.out.println("cat: Error reading file");
	}

	return true;
    }

    @Override
    public String toString() {
	return "Display contents of an .lpc file";
    }
}
