package io.github.protasm.lpc2j.parser;

import io.github.protasm.lpc2j.parser.parselet.InfixParselet;
import io.github.protasm.lpc2j.parser.parselet.PrefixParselet;

public class ParseRule {
    private final PrefixParselet prefix;
    private final InfixParselet infix;
    private final int precedence;

    public ParseRule(PrefixParselet prefix, InfixParselet infix, int precedence) {
	this.prefix = prefix;
	this.infix = infix;
	this.precedence = precedence;
    }

    public PrefixParselet prefix() {
	return prefix;
    }

    public InfixParselet infix() {
	return infix;
    }

    public int precedence() {
	return precedence;
    }
}
