package io.github.protasm.lpc2j.console.cmd;

import java.io.File;

import io.github.protasm.lpc2j.console.Console;

public class CmdDirChange extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	if (args.length == 0) {
	    System.out.println("Usage: cd <directory>");

	    return true;
	}

	try {
	    File base = new File(console.baseDir());
	    File newPath = new File(console.baseDir(), new File(console.pwd(), args[0]).toString()).getCanonicalFile();

	    if (!newPath.exists() || !newPath.isDirectory()) {
		System.out.println("No such directory: " + args[0]);

		return true;
	    }

	    if (!newPath.getCanonicalPath().startsWith(base.getCanonicalPath())) {
		System.out.println("Permission denied.");

		return true;
	    }

	    console.setPWD(newPath.getCanonicalPath().substring(base.getCanonicalPath().length()));
	} catch (Exception e) {
	    System.out.println("Error changing directory.");
	}

	return true;
    }

    @Override
    public String toString() {
	return "Change the working directory";
    }
}