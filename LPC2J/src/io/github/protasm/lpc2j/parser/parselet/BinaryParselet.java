package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.BinaryOpType;
import io.github.protasm.lpc2j.parser.ParseRule;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.TokenType;

import static io.github.protasm.lpc2j.scanner.TokenType.*;

public class BinaryParselet implements Parselet {
    @Override
    public void parse(Parser parser, boolean canAssign, boolean inBinaryOp) {
	TokenType operatorType = parser.tokens().previous().tType();
	ParseRule rule = parser.getRule(operatorType);

	// evaluate and load RHS operand
	parser.parsePrecedence(rule.precedence() + 1, true);

	switch (operatorType) {
	case T_PLUS:
	    parser.binaryOp(BinaryOpType.BOP_ADD);

	    break;
	case T_MINUS:
	    parser.binaryOp(BinaryOpType.BOP_SUB);

	    break;
	case T_STAR:
	    parser.binaryOp(BinaryOpType.BOP_MULT);

	    break;
	case T_SLASH:
	    parser.binaryOp(BinaryOpType.BOP_DIV);

	    break;
	default:
	    return;
	} // switch (operatorType)
    }
}