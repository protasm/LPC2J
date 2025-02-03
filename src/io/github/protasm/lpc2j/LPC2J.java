package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.console.Console;

public class LPC2J {
    public static void main(String... args) {
	Console console = new Console();
	
	console.repl();
    }
}