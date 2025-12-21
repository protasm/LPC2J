package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;

public class CmdIR extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        System.out.println("IR");

        if (args.length < 1) {
            System.out.println("Error: No fileName specified.");

            return true;
        }

        String vPathStr = pathStrOfArg(console.vPath(), args[0]);
        FSSourceFile sf = console.ir(vPathStr);

        if (sf == null) {
            return true;
        }

        new IRPrinter(System.out).print(sf.typedIr());

        return true;
    }

    @Override
    public String toString() {
        return "Lower to IR <source fileName>";
    }
}
