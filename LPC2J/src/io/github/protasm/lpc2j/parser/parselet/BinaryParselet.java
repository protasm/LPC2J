package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.ParseRule;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.TokenType;

public class BinaryParselet implements Parselet {
    @Override
    public void parse(Parser parser, LPC2J compiler, boolean canAssign) {
	TokenType operatorType = parser.previous().type();
	ParseRule rule = parser.getRule(operatorType);

	// right HERE we need to know if we have to type-promote LHS

	// evaluate and load RHS operand
	parser.parsePrecedence(rule.precedence() + 1);

	switch (operatorType) {
	case TOKEN_PLUS:
	    compiler.binaryOp(LPC2J.Operation.ADD);

	    break;
	case TOKEN_MINUS:
	    compiler.binaryOp(LPC2J.Operation.SUB);

	    break;
	case TOKEN_STAR:
	    compiler.binaryOp(LPC2J.Operation.MULT);

	    break;
	case TOKEN_SLASH:
	    compiler.binaryOp(LPC2J.Operation.DIV);

	    break;
	default:
	    return;
	} // switch (operatorType)
    }
}