package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.token.TokenType.T_INT_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS_MINUS;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS_PLUS;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_ARROW;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedAssignment;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedCall;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedInvoke;
import io.github.protasm.lpc2j.parser.type.AssignOpType;
import io.github.protasm.lpc2j.token.Token;

public class PrefixIdentifier implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        int line = parser.currLine();
        String identifier = parser.tokens().previous().lexeme();

        if (parser.tokens().check(T_LEFT_PAREN)) {
            ASTArguments args = parser.arguments();
            return new ASTExprUnresolvedCall(line, identifier, args);
        }

        if (parser.tokens().match(T_RIGHT_ARROW)) {
            Token<String> nameToken = parser.tokens().consume(T_IDENTIFIER, "Expect method name.");
            return new ASTExprUnresolvedInvoke(line, identifier, nameToken.lexeme(), parser.arguments());
        }

        if (canAssign && parser.tokens().match(T_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.SET, parser.expression());
        else if (canAssign && parser.tokens().match(T_PLUS_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.ADD, parser.expression());
        else if (canAssign && parser.tokens().match(T_MINUS_EQUAL))
            return new ASTExprUnresolvedAssignment(line, identifier, AssignOpType.SUB, parser.expression());
        else if (canAssign && parser.tokens().match(T_PLUS_PLUS))
            return new ASTExprUnresolvedAssignment(
                    line,
                    identifier,
                    AssignOpType.ADD,
                    new ASTExprLiteralInteger(line, new Token<>(T_INT_LITERAL, "1", 1, null)));
        else if (canAssign && parser.tokens().match(T_MINUS_MINUS))
            return new ASTExprUnresolvedAssignment(
                    line,
                    identifier,
                    AssignOpType.SUB,
                    new ASTExprLiteralInteger(line, new Token<>(T_INT_LITERAL, "1", 1, null)));

        return new ASTExprUnresolvedIdentifier(line, identifier);
    }
}
