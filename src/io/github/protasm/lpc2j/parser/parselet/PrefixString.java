package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprStringLiteral;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.Token;

public class PrefixString implements PrefixParselet {
	@SuppressWarnings("unchecked")
	@Override
	public ASTExpression parse(Parser parser, boolean canAssign) {
		return new ASTExprStringLiteral(parser.currLine(), (Token<String>) parser.tokens().previous());
	}
}
