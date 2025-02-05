package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;
import io.github.protasm.lpc2j.parser.ast.ASTObject;

public class CmdParse extends Command {
	@Override
	public boolean execute(Console console, String... args) {
		System.out.println("Parse");

		if (args.length < 1) {
			System.out.println("Error: No file specified.");

			return false;
		}

		ASTObject ast = console.parse(args[0]);

		if (ast != null)
			System.out.println(ast);

		return false;
	}
}
