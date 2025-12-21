package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;

public class CmdAnalyze extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        System.out.println("Analyze");

        if (args.length < 1) {
            System.out.println("Error: No fileName specified.");

            return true;
        }

        String vPathStr = pathStrOfArg(console.vPath(), args[0]);
        FSSourceFile sf = console.analyze(vPathStr);

        if (sf == null) {
            return true;
        }

        new SemanticModelPrinter(System.out).print(sf.semanticModel());

        return true;
    }

    @Override
    public String toString() {
        return "Analyze <source fileName>";
    }
}
