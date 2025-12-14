package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.InvalidPathException;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.VirtualFileServer;

public class CmdFileCat extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        VirtualFileServer basePath = console.basePath();

        if (args.length == 0) {
            System.out.println("Usage: cat [<fileName.lpc> | <fileName.c>]");

            return true;
        }

        try {
            String vPathStr = pathStrOfArg(console.vPath(), args[0]);
            String contents = basePath.contentsOfFileAt(vPathStr);

            if (contents == null) {
                System.out.println("Invalid fileName: " + args[0]);

                return true;
            }

            System.out.println(contents);
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
