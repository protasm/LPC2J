package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;

public class CmdQuit extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	System.out.println("Goodbye.");

	return true;
    }
}
