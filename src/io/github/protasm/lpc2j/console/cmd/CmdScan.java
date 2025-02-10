package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.fs.FSSourceFile;
import io.github.protasm.lpc2j.scanner.Tokens;

public class CmdScan extends Command {
	@Override
	public boolean execute(Console console, String... args) {
		System.out.println("Scan");

		if (args.length < 1) {
			System.out.println("Error: No file specified.");

			return false;
		}

		FSSourceFile sf = console.scan(args[0]);

		if (sf == null)
			return false;

		Tokens tokens = sf.tokens();

		if (tokens != null)
			System.out.println(tokens);

		return false;
	}

	@Override
	public String toString() {
		return "Scan <source file>";
	}
}
