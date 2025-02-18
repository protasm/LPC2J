package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.fs.FSBasePath;

public class CmdDirChange extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	FSBasePath basePath = console.basePath();

	if (args.length == 0) {
	    console.setVPath(Path.of("/"));

	    return true;
	}

	Path argPath = Path.of(args[0]);
	Path newPath;

	// handle relative path argument
	if (!argPath.isAbsolute() && (console.vPath() != null))
	    argPath = Paths.get(console.vPath().toString(), args[0]);

	newPath = basePath.dirAt(argPath.toString());

	if (newPath != null)
	    console.setVPath(newPath);
	else
	    System.out.println("Invalid path: " + args[0]);

	return true;
    }

    @Override
    public String toString() {
	return "Change the working directory";
    }
}