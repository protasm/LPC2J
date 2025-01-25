package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.LPC2J;
import static io.github.protasm.lpc2j.LiteralType.*;
import io.github.protasm.lpc2j.parser.Parser;

public class LiteralParselet implements Parselet {
    @Override
    public void parse(Parser parser, LPC2J compiler, boolean canAssign, boolean inBinaryOp) {
	switch (parser.previous().tType()) {
	case TOKEN_TRUE:
	    compiler.literal(LT_TRUE);

	    break;
	case TOKEN_FALSE:
	    compiler.literal(LT_FALSE);

	    break;
	case TOKEN_NIL:
	    compiler.literal(LT_NULL);

	    break;
	default:
	    break;
	}
    }
}