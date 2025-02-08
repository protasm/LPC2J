package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.PrattParser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.TokenType;

import static io.github.protasm.lpc2j.UnaryOpType.*;

public class PrefixUnaryOp implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
	int line = parser.currLine();
	TokenType opType = parser.tokens().previous().tType();
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