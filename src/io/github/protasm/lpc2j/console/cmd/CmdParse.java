package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.fs.FSSourceFile;
import io.github.protasm.lpc2j.parser.ast.ASTObject;

public class CmdParse extends Command {
	@Override
	public boolean execute(Console console, String... args) {
		System.out.println("Parse");

		if (args.length < 1) {
			System.out.println("Error: No file specified.");

			return false;
		}

		FSSourceFile sf = console.parse(args[0]);

		if (sf == null)
			return false;

		ASTObject astObject = sf.astObject();

		if (astObject != null)
			System.out.println(astObject);

		return false;
	}

	@Override
	public String toString() {
		return "Parse <source file>";
	}
}
