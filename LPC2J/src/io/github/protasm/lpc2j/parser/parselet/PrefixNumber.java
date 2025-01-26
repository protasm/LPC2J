package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIntegerLiteral;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.Token;
import io.github.protasm.lpc2j.scanner.TokenType;

public class PrefixNumber implements PrefixParselet {
    @SuppressWarnings("unchecked")
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
//	Object literal = parser.tokens().previous().literal();
//	TokenType numType = parser.tokens().previous().tType();
//	JType lhsType = compiler.cb().currMethod().operandTypes().peek();

	TokenType tType = parser.tokens().previous().tType();
	
	switch (tType) {
	case T_INT_LITERAL:
	    return new ASTExprIntegerLiteral(parser.currLine(), (Token<Integer>) parser.tokens().previous());
//	case TOKEN_NUM_FLOAT:
////	    if (inBinaryOp && lhsType == JType.JINT) {
////		compiler.i2f();
////	    }
////
////	    compiler.lpcFloat((Float) literal);
//
//	    return;
	default:
	    return null;
	} // switch (numType)
    }
}
