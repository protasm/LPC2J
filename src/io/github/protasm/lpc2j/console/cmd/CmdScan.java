package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.scanner.Tokens;

public class CmdScan extends Command {
	@Override
	public boolean execute(Console console, String... args) {
		System.out.println("Scan");

		if (args.length < 1) {
			System.out.println("Error: No file specified.");

			return false;
		}

		Tokens tokens = console.scan(args[0]);

		if (tokens != null)
			System.out.println(tokens);

		return false;
	}
}
