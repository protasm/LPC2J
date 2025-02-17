package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.protasm.lpc2j.console.Console;

public class CmdDirChange extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	if (args.length == 0) {
	    console.setVPath(Path.of("/"));

	    return true;
	}

	try {
	    Path vPath = Path.of(args[0]);
	    Path newPath;

	    if (!vPath.isAbsolute() && (console.vPath() != null))
		vPath = Paths.get(console.vPath().toString(), args[0]);

	    newPath = console.basePath().resolve(vPath);

	    console.setVPath(newPath);
	} catch (InvalidPathException e) {
	    System.out.println(e);
	}

	return true;
    }

    @Override
    public String toString() {
	return "Change the working directory";
    }
}