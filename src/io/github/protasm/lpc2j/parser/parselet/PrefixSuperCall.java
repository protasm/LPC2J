package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.token.TokenType.T_IDENTIFIER;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedParentCall;
import io.github.protasm.lpc2j.token.Token;

public final class PrefixSuperCall implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        Token<String> nameToken = parser.tokens().consume(T_IDENTIFIER, "Expect inherited method name.");
        ASTArguments arguments = parser.arguments();
        return new ASTExprUnresolvedParentCall(parser.currLine(), nameToken.lexeme(), arguments);
    }
}
