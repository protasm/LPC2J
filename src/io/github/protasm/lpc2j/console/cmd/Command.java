package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;

public abstract class Command {
	public abstract boolean execute(Console console, String... args);

	@Override
	public abstract String toString();
}
