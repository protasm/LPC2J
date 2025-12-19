package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.Path;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.VirtualFileServer;

public class CmdDirChange extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        VirtualFileServer vfs = console.vfs();

        if (args.length == 0) {
            console.setVPath(Path.of("/"));

            return true;
        }

        String vPathStr = pathStrOfArg(console.vPath(), args[0]);
        Path newPath = vfs.dirAt(vPathStr);

        if (newPath != null) {
            console.setVPath(newPath);
        } else {
            System.out.println("Invalid path: " + args[0]);
        }

        return true;
    }

    @Override
    public String toString() {
        return "Change the working directory";
    }
}
