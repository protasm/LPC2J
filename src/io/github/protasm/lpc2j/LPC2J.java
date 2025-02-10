package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.console.Console;

public class LPC2J {
	public static void main(String... args) {
		if (args.length != 1) {
			System.out.println("Error: missing base path.");

			System.exit(-1);
		}

		Console console = new Console(args[0]);

		console.repl();
	}
}