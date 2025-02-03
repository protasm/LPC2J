package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;

public class CmdHelp extends Command {
	@Override
	public boolean execute(Console console, String... args) {
		System.out.println("Help");

		return false;
	}
}
