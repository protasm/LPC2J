package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.console.Console;

public class CmdLoad extends Command {
    @Override
    public void execute(Console console, String... args) {
	System.out.println("Load");
	
	for (String arg : args)
	    System.out.println(arg);
    }
}
