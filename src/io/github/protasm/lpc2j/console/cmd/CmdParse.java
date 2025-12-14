package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;

public class CmdParse extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        System.out.println("Parse");

        if (args.length < 1) {
            System.out.println("Error: No fileName specified.");

            return true;
        }

        String vPathStr = pathStrOfArg(console.vPath(), args[0]);
        FSSourceFile sf = console.parse(vPathStr);

        if (sf == null) {
            return true;
        }

        sf.astObject().accept(new PrintVisitor());

        return true;
    }

    @Override
    public String toString() {
        return "Parse <source fileName>";
    }
}
