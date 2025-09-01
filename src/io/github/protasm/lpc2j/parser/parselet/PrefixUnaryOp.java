package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.parser.type.UnaryOpType.UOP_NEGATE;
import static io.github.protasm.lpc2j.parser.type.UnaryOpType.UOP_NOT;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.PrattParser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.token.TokenType;

public class PrefixUnaryOp implements PrefixParselet {
	@Override
	public ASTExpression parse(Parser parser, boolean canAssign) {
		int line = parser.currLine();
		TokenType opType = parser.tokens().previous().type();
		ASTExpression expr = parser.parsePrecedence(PrattParser.Precedence.PREC_UNARY);

		switch (opType) {
		case T_BANG:
			return new ASTExprOpUnary(line, expr, UOP_NOT);
		case T_MINUS:
			return new ASTExprOpUnary(line, expr, UOP_NEGATE);
		default:
			return null; // TODO throw exception
		}
	}
}