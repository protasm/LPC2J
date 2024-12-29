package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.parser.Parser.Precedence.PREC_UNARY;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.TokenType;

public class UnaryParselet implements Parselet {

	@Override
	public void parse(Parser parser, LPC2J compiler, boolean canAssign) {
		TokenType operatorType = parser.previous().type();

		parser.parsePrecedence(PREC_UNARY);

		switch (operatorType) {
		case TOKEN_BANG:

			break;
		case TOKEN_MINUS:
			compiler.negate();

			break;
		default:
			return;
		}
	}
}