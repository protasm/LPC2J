package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.LPCConsole;

public class CmdHelp extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        System.out.println("===========================================");
        System.out.println("[pp] preprocess -> [s] scan -> [p] parse ->");
        System.out.println("[a] analyze -> [ir] ir -> [c] compile -> [l] load");
        System.out.println("===========================================\n");

        LPCConsole.commands().forEach((cmd, aliases) -> {
            System.out.printf("%-20s -> %s%n", String.join(", ", aliases), cmd);
        });

        return true;
    }

    @Override
    public String toString() {
        return "Display list of available commands";
    }
}
