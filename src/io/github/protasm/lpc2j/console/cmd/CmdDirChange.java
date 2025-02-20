package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.Path;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.console.fs.FSBasePath;

public class CmdDirChange extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	FSBasePath basePath = console.basePath();

	if (args.length == 0) {
	    console.setVPath(Path.of("/"));

	    return true;
	}

	String vPathStr = pathStrOfArg(console.vPath(), args[0]);
	Path newPath = basePath.dirAt(vPathStr);

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