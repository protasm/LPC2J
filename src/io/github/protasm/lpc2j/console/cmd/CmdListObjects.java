package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.LPCConsole;

public class CmdListObjects extends Command {
	@Override
	public boolean execute(LPCConsole console, String... args) {
		System.out.println("List Objects");

		System.out.println(console.objects().keySet());

		return true;
	}

	@Override
	public String toString() {
		return "List loaded objects";
	}
}
