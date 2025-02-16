package io.github.protasm.lpc2j.console.cmd;

import java.io.File;
import java.util.Arrays;

import io.github.protasm.lpc2j.console.Console;

public class CmdDirList extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	File dir = targetDir(console, args);

	if (dir == null)
	    return true;

	File[] files = validFiles(dir);

	if (files == null)
	    return true;

	printFiles(files);

	return true;
    }

    private File targetDir(Console console, String... args) {
	File dir = args.length > 0
		? new File(console.baseDir(), new File(console.pwd(), args[0]).toString())
		: new File(console.baseDir(), console.pwd());

	if (!dir.exists() || !dir.isDirectory()) {
	    System.out.println("ls: No such directory: " + (args.length > 0 ? args[0] : ""));

	    return null;
	}

	return dir;
    }

    private File[] validFiles(File dir) {
	File[] files = dir.listFiles((file) -> file.isDirectory() || file.getName().endsWith(".lpc"));

	if (files == null)
	    System.out.println("ls: Unable to list directory");

	return files;
    }

    private void printFiles(File[] files) {
	Arrays.stream(files).map(file -> file.isDirectory() ? file.getName() + "/" : file.getName())
		.forEach(System.out::println);
    }

    @Override
    public String toString() {
	return "List .lpc files and directories";
    }
}
