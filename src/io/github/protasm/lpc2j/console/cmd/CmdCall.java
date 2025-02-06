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
		Object[] objArgs = inferArgTypes(strArgs);

		console.call(args[0], args[1], objArgs);

		return false;
	}
	
	private Object[] inferArgTypes(String[] strArgs) {
		Object[] objArgs = new Object[strArgs.length];
		
		for (int i = 0; i < strArgs.length; i++) {
			String strArg = strArgs[i];
			
		    try { objArgs[i] = Integer.parseInt(strArg); continue; } catch (NumberFormatException ignored) {}

		    if ("true".equalsIgnoreCase(strArg) || "false".equalsIgnoreCase(strArg)) {
		        objArgs[i] = Boolean.parseBoolean(strArg);
		        
		        continue;
		    }

		    objArgs[i] = strArg;
		}
		
		return objArgs;
	}

	@Override
	public String toString() {
		return "Call <object> <method> [<args>]";
	}
}
