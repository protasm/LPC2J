package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.Parser;

public class StringParselet implements Parselet {
    @Override
    public void parse(Parser parser, LPC2J compiler, boolean canAssign, boolean inBinaryOp) {
	String value = (String) parser.previous().literal();

	compiler.lpcString(value);
    }
}
