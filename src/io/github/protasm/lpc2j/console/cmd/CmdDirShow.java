package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;

public class CmdDirShow extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	System.out.println(console.pwd());

	return true;
    }

    @Override
    public String toString() {
	return "Print the current working directory";
    }
}