package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.JType;
import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.TokenType;

public class NumberParselet implements Parselet {
    @Override
    public void parse(Parser parser, LPC2J compiler, boolean canAssign, boolean inBinaryOp) {
	Object literal = parser.previous().literal();
	TokenType numType = parser.previous().type();
	JType lhsType = compiler.cb().mb().operandTypes().peek();

	switch (numType) {
	case TOKEN_NUM_INT:
	    compiler.lpcInteger((Integer) literal);

	    if (inBinaryOp && lhsType == JType.JFLOAT)
		compiler.i2f();

	    return;
	case TOKEN_NUM_FLOAT:
	    if (inBinaryOp && lhsType == JType.JINT)
		compiler.i2f();

	    compiler.lpcFloat((Float) literal);

	    return;
	default:
	    break;
	} // switch (numType)
    }
}
