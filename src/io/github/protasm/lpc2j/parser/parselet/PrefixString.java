package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.Token;

public class PrefixString implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
	Token<String> previous = parser.tokens().previous();

	return new ASTExprLiteralString(parser.currLine(), previous);
    }
}
