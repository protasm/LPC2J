package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;
import io.github.protasm.lpc2j.semantic.SemanticModel;
import io.github.protasm.lpc2j.semantic.SemanticScope;
import io.github.protasm.lpc2j.parser.ast.Symbol;

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

        printSemanticModel(sf.semanticModel());

        return true;
    }

    private void printSemanticModel(SemanticModel semanticModel) {
        System.out.println("SemanticModel:");
        SemanticScope scope = semanticModel.objectScope();
        if (scope.symbols().isEmpty()) {
            System.out.println("  (no symbols)");
            return;
        }

        scope.symbols().forEach((name, symbol) -> printSymbol(name, symbol));
    }

    private void printSymbol(String name, Symbol symbol) {
        System.out.printf("  %-15s -> %s%n", name, symbol);
    }

    @Override
    public String toString() {
        return "Analyze <source fileName>";
    }
}
