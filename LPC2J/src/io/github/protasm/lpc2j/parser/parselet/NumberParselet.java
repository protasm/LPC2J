package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.TokenType;

public class NumberParselet implements Parselet {
    @Override
    public void parse(Parser parser, LPC2J compiler, boolean canAssign) {
	Object literal = parser.previous().literal();
	TokenType numType = parser.previous().type();

	switch (numType) {
	case TOKEN_NUM_INT:
	    compiler.lpcInteger((Integer) literal);

	    return;
	case TOKEN_NUM_FLOAT:
	    compiler.lpcFloat((Float) literal);

	    return;
	default:
	    break;
	}
    }
}