package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.InvalidPathException;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.VirtualFileServer;

public class CmdFileCat extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        VirtualFileServer vfs = console.vfs();

        if (args.length == 0) {
            System.out.println("Usage: cat [<fileName.lpc> | <fileName.c>]");

            return true;
        }

        try {
            String vPathStr = pathStrOfArg(console.vPath(), args[0]);
            String contents = vfs.contentsOfFileAt(vPathStr);

            if (contents == null) {
                System.out.println("Invalid fileName: " + args[0]);

                return true;
            }

            String[] lines = contents.split("\\R", -1);

            for (int i = 0; i < lines.length; i++) {
                System.out.printf("%d: %s%n", i + 1, lines[i]);
            }
        } catch (InvalidPathException e) {
            System.out.println("Error displaying contents of fileName: " + args[0]);
        }

        return true;
    }

    @Override
    public String toString() {
        return "Display contents of a source fileName";
    }
}
