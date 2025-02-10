package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralTrue;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class PrefixLiteral implements PrefixParselet {
	@Override
	public ASTExpression parse(Parser parser, boolean canAssign) {
		switch (parser.tokens().previous().tType()) {
		case T_TRUE:
			return new ASTExprLiteralTrue(parser.currLine());
		case T_FALSE:
			return new ASTExprLiteralFalse(parser.currLine());
		case T_NIL:
			return null; // TODO
		default:
			return null; // TODO
		}
	}
}