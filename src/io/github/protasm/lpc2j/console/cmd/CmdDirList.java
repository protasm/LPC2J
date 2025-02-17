package io.github.protasm.lpc2j.console.cmd;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.fs.FSBasePath;

public class CmdDirList extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	FSBasePath basePath = console.basePath();
	File[] files;

	try {
	    if (args.length == 0)
		files = basePath.filesIn(console.vPath());
	    else {
		Path path = Path.of(args[0]);
		Path resolved = basePath.resolve(path);

		files = basePath.filesIn(resolved);
	    }

	    files = validFiles(files);

	    if (files.length == 0)
		return true;

	    printFiles(files);
	} catch (InvalidPathException e) {
	    System.out.println(e);
	}

	return true;
    }

    private File[] validFiles(File[] files) {
	FileFilter ff = file -> file.isDirectory()
		|| file.getName().endsWith(".lpc")
		|| file.getName().endsWith(".c");

	return Arrays.stream(files)
		.filter(ff::accept)
		.toArray(File[]::new);
    }

    private void printFiles(File[] files) {
	Arrays.stream(files).map(
		file -> file.isDirectory()
			? file.getName() + "/"
			: file.getName())
		.forEach(System.out::println);
    }

    @Override
    public String toString() {
	return "List source files and directories";
    }
}
