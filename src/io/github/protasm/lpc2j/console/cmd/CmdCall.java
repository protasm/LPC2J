package io.github.protasm.lpc2j.console.cmd;

import java.util.Arrays;

import io.github.protasm.lpc2j.console.LPCConsole;

public class CmdCall extends Command {
	@Override
	public boolean execute(LPCConsole console, String... args) {
		if (args.length < 2) {
			System.out.println("Usage:  call <object> <method> [<args>]");

			return true;
		}

		String[] strArgs = Arrays.copyOfRange(args, 2, args.length);
		Object[] objArgs = inferArgTypes(console, strArgs);

		Object result = console.call(args[0], args[1], objArgs);
		System.out.println(String.valueOf(result));

		return true;
	}

	private Object[] inferArgTypes(LPCConsole console, String[] strArgs) {
		Object[] objArgs = new Object[strArgs.length];

		for (int i = 0; i < strArgs.length; i++) {
			String strArg = strArgs[i];

			// Integer?
			try {
				objArgs[i] = Integer.parseInt(strArg);
				continue;
			} catch (NumberFormatException ignored) {
			}

			// Boolean?
			if ("true".equalsIgnoreCase(strArg) || "false".equalsIgnoreCase(strArg)) {
				objArgs[i] = Boolean.parseBoolean(strArg);

				continue;
			}

			// Loaded object?
			if (console.objects().containsKey(strArg)) {
				objArgs[i] = console.objects().get(strArg);

				continue;
			}

			// String.
			objArgs[i] = strArg;
		}

		return objArgs;
	}

	@Override
	public String toString() {
		return "Call <object> <method> [<args>]";
	}
}
