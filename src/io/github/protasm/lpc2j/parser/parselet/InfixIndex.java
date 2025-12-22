package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_BRACKET;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayStore;

public class InfixIndex implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
        int line = parser.currLine();
        ASTExpression index = parser.expression();

        parser.tokens().consume(T_RIGHT_BRACKET, "Expect ']' after array element index.");

        if (canAssign && parser.tokens().match(T_EQUAL))
            return new ASTExprArrayStore(line, left, index, parser.expression());

        return new ASTExprArrayAccess(line, left, index);
    }
}
