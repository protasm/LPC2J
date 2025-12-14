package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.LPCConsole;

public class CmdQuit extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        System.out.println("Goodbye.");

        return false;
    }

    @Override
    public String toString() {
        return "Quit";
    }
}
