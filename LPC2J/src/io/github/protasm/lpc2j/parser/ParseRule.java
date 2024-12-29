package io.github.protasm.lpc2j.parser;

import io.github.protasm.lpc2j.parser.parselet.Parselet;

public class ParseRule {

	private Parselet prefix;

	private Parselet infix;

	private int precedence;

	public ParseRule(Parselet prefix, Parselet infix, int precedence) {
		this.prefix = prefix;
		this.infix = infix;
		this.precedence = precedence;
	}

	public Parselet prefix() {
		return prefix;
	}

	public Parselet infix() {
		return infix;
	}

	public int precedence() {
		return precedence;
	}
}
