package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.token.TokenList;
import io.github.protasm.lpc2j.console.LPCConsole;
import io.github.protasm.lpc2j.console.fs.FSSourceFile;

public class CmdScan extends Command {
	@Override
	public boolean execute(LPCConsole console, String... args) {
		System.out.println("Scan");

		if (args.length < 1) {
			System.out.println("Error: No fileName specified.");

			return true;
		}

		String vPathStr = pathStrOfArg(console.vPath(), args[0]);
		FSSourceFile sf = console.scan(vPathStr);

		if (sf == null) {
			return true;
		}

		TokenList tokens = sf.tokens();

		if (tokens != null) {
			System.out.println(tokens);
		}

		return true;
	}

	@Override
	public String toString() {
		return "Scan <source fileName>";
	}
}
