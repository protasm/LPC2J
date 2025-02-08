package io.github.protasm.lpc2j.console.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.protasm.lpc2j.console.Console;

public class CmdCall extends Command {
    @Override
    public boolean execute(Console console, String... args) {
	if (args.length < 2) {
	    System.out.println("Usage:  call <object> <method> [<args>]");

	    return false;
	}

	String[] strArgs = Arrays.copyOfRange(args, 2, args.length);
	Object[] objArgs = inferArgTypes(console, strArgs);

	console.call(args[0], args[1], objArgs);

	return false;
    }

    private Object[] inferArgTypes(Console console, String[] strArgs) {
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
