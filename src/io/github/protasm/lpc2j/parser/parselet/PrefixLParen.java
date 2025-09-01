package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_PAREN;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class PrefixLParen implements PrefixParselet {
	@Override
	public ASTExpression parse(Parser parser, boolean canAssign) {
//    if (parser.match(TOKEN_LEFT_BRACE)) {
//      int elementCount = compiler.array();
//
//      compiler.emitCode(OP_ARRAY);
//      compiler.emitCode(elementCount);
//    } else if (parser.match(TOKEN_LEFT_BRACKET)) {
//      int entryCount = compiler.mapping();
//
//      compiler.emitCode(OP_MAPPING);
//      compiler.emitCode(entryCount);
//    } else {
		ASTExpression expr = parser.expression();

		parser.tokens().consume(T_RIGHT_PAREN, "Expect ')' after expression.");

		return expr;
//    }
	}
}