package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.LPCConsole;

public class CmdDirShow extends Command {
    @Override
    public boolean execute(LPCConsole console, String... args) {
        System.out.println(console.pwd());

        return true;
    }

    @Override
    public String toString() {
        return "Print the current working directory";
    }
}
