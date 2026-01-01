package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenType;

public class PrefixNumber implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        TokenType tType = parser.tokens().previous().type();

        switch (tType) {
        case T_INT_LITERAL:
            Token<Integer> previous = parser.tokens().previous();

            return new ASTExprLiteralInteger(parser.currLine(), previous);
//    case TOKEN_NUM_FLOAT:
        //// if (inBinaryOp && lhsType == JType.JINT) {
////        compiler.i2f();
////        }
////
////        compiler.lpcFloat((Float) literal);
//
//        return;
        default:
            return null;
        } // switch (numType)
    }
}
