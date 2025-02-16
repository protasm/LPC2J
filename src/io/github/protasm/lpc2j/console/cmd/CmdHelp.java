package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;

public class CmdHelp extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	Console.commands().forEach((key, value) -> System.out.println(key + "\t-> " + value));

	return true;
    }

    @Override
    public String toString() {
	return "Display list of available commands";
    }
}
