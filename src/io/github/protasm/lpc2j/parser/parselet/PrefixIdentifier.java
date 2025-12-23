package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_ARROW;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierCall;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierStore.AssignmentOp;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeIdentifier;
import io.github.protasm.lpc2j.token.Token;

public class PrefixIdentifier implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        int line = parser.currLine();
        String identifier = parser.tokens().previous().lexeme();

        // Call?
        if (parser.tokens().check(T_LEFT_PAREN)) {
            ASTArguments args = parser.arguments();

            return new ASTExprIdentifierCall(line, identifier, args);
        }

        // Dynamic invoke on a target identifier.
        if (parser.tokens().match(T_RIGHT_ARROW)) {
            Token<String> nameToken = parser.tokens().consume(T_IDENTIFIER, "Expect method name.");
            return new ASTExprInvokeIdentifier(line, identifier, nameToken.lexeme(), parser.arguments());
        }

        if (canAssign && parser.tokens().match(T_EQUAL))
            return new ASTExprIdentifierStore(line, identifier, AssignmentOp.ASSIGN, parser.expression());

        if (canAssign && parser.tokens().match(T_PLUS_EQUAL))
            return new ASTExprIdentifierStore(line, identifier, AssignmentOp.PLUS_EQUAL, parser.expression());

        if (canAssign && parser.tokens().match(T_MINUS_EQUAL))
            return new ASTExprIdentifierStore(line, identifier, AssignmentOp.MINUS_EQUAL, parser.expression());

        return new ASTExprIdentifierAccess(line, identifier);
    }
}
