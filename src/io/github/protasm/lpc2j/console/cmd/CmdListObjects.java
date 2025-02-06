package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;

public class CmdListObjects extends Command {
	@Override
	public boolean execute(Console console, String... args) {
		System.out.println("List Objects");
		System.out.println(console.objects().keySet());

		return false;
	}

	@Override
	public String toString() {
		return "List loaded objects";
	}
}
