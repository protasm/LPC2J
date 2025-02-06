package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.fs.FSSourceFile;

public class CmdLoad extends Command {
	@Override
	public boolean execute(Console console, String... args) {
		System.out.println("Load");

		if (args.length < 1) {
			System.out.println("Error: No file specified.");

			return false;
		}

		FSSourceFile sf = console.load(args[0]);

		if (sf == null)
			return false;

		console.objects().put(sf.dotName(), sf.lpcObject());

		System.out.println(sf.dotName() + " loaded.");

		return false;
	}

	@Override
	public String toString() {
		return "Load <source file>";
	}
}
