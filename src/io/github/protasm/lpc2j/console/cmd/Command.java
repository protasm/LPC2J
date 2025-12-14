package io.github.protasm.lpc2j.console.cmd;

import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.protasm.lpc2j.console.LPCConsole;

public abstract class Command {
	public abstract boolean execute(LPCConsole console, String... args);

	protected String pathStrOfArg(Path consoleVPath, String arg) {
		Path vPath = Path.of(arg);

		// handle relative path argument
		if (!vPath.isAbsolute() && (consoleVPath != null)) {
			vPath = Paths.get(consoleVPath.toString(), arg);
		}

		return vPath.toString();
	}

	@Override
	public abstract String toString();
}
