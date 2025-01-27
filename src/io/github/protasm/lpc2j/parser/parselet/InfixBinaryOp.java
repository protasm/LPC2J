package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.ParseRule;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.TokenType;

import static io.github.protasm.lpc2j.BinaryOpType.*;

public class InfixBinaryOp implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
	int line = parser.currLine();
	TokenType operatorType = parser.tokens().previous().tType();
	ParseRule rule = parser.getRule(operatorType);

	// evaluate and load RHS operand
	ASTExpression right = parser.parsePrecedence(rule.precedence() + 1);

	switch (operatorType) {
	case T_PLUS:
	    return new ASTExprOpBinary(line, left, right, BOP_ADD);
	case T_MINUS:
	    return new ASTExprOpBinary(line, left, right, BOP_SUB);
	case T_STAR:
	    return new ASTExprOpBinary(line, left, right, BOP_MULT);
	case T_SLASH:
	    return new ASTExprOpBinary(line, left, right, BOP_DIV);
	default:
	    return null;
	} // switch (operatorType)
    }
}
