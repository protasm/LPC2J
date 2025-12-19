package io.github.protasm.lpc2j.console.cmd;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.Comparator;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.VirtualFileServer;

public class CmdDirList extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        VirtualFileServer vfs = console.vfs();
        File[] files;

        try {
            if (args.length == 0) {
                files = vfs.filesIn(console.vPath().toString());
            } else {
                String vPathStr = pathStrOfArg(console.vPath(), args[0]);
                files = vfs.filesIn(vPathStr);
            }

            if (files == null) {
                System.out.println("Invalid path: " + args[0]);

                return true;
            }

            files = validFiles(files);

            if (files.length > 0) {
                printFiles(files);
            }
        } catch (InvalidPathException e) {
            System.out.println("Invalid path: " + args[0]);
        }

        return true;
    }

    private File[] validFiles(File[] files) {
        FileFilter ff = file -> file.isDirectory() || file.getName().endsWith(".lpc") || file.getName().endsWith(".c");

        return Arrays.stream(files).filter(ff::accept).toArray(File[]::new);
    }

    private void printFiles(File[] files) {
        Comparator<File> fileComparator = Comparator
                // Directories should come first: directories return false, files true.
                .comparing((File f) -> !f.isDirectory())
                // Within each group, sort alphabetically (case-insensitive)
                .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER);

        Arrays.stream(files).sorted(fileComparator)
                .map(file -> file.isDirectory() ? file.getName() + "/" : file.getName()).forEach(System.out::println);
    }

    @Override
    public String toString() {
        return "List source files and directories";
    }
}
