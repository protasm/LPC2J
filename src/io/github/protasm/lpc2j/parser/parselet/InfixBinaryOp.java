package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_ADD;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_AND;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_DIV;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_EQ;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_NE;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_GE;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_GT;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_LE;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_LT;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_MULT;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_OR;
import static io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_SUB;

import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.ParseRule;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.PrattParser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.token.Token;

public class InfixBinaryOp implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
        int line = parser.currLine();
        Token<?> previous = parser.tokens().previous();
        ParseRule rule = PrattParser.getRule(previous);

        // evaluate and load RHS operand
        ASTExpression right = parser.parsePrecedence(rule.precedence() + 1);

        switch (previous.type()) {
        case T_PLUS:
            return new ASTExprOpBinary(line, left, right, BOP_ADD);
        case T_MINUS:
            return new ASTExprOpBinary(line, left, right, BOP_SUB);
        case T_STAR:
            return new ASTExprOpBinary(line, left, right, BOP_MULT);
        case T_SLASH:
            return new ASTExprOpBinary(line, left, right, BOP_DIV);
        case T_DBL_PIPE:
            return new ASTExprOpBinary(line, left, right, BOP_OR);
        case T_DBL_AMP:
            return new ASTExprOpBinary(line, left, right, BOP_AND);
        case T_GREATER:
            return new ASTExprOpBinary(line, left, right, BOP_GT);
        case T_GREATER_EQUAL:
            return new ASTExprOpBinary(line, left, right, BOP_GE);
        case T_LESS:
            return new ASTExprOpBinary(line, left, right, BOP_LT);
        case T_LESS_EQUAL:
            return new ASTExprOpBinary(line, left, right, BOP_LE);
        case T_EQUAL_EQUAL:
            return new ASTExprOpBinary(line, left, right, BOP_EQ);
        case T_BANG_EQUAL:
            return new ASTExprOpBinary(line, left, right, BOP_NE);
        default:
            throw new ParseException("Unknown operator type.", parser.tokens().current());
        } // switch (operatorType)
    }
}
