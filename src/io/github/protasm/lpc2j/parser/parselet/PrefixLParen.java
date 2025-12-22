package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.token.TokenType.T_COLON;
import static io.github.protasm.lpc2j.token.TokenType.T_COMMA;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_BRACKET;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_BRACKET;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_PAREN;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprMappingEntry;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprMappingLiteral;
import java.util.ArrayList;
import java.util.List;

public class PrefixLParen implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        if (parser.tokens().match(T_LEFT_BRACKET)) {
            ASTExpression mapping = parseMappingLiteral(parser);
            parser.tokens().consume(T_RIGHT_PAREN, "Expect ')' after mapping literal.");
            return mapping;
        }

        ASTExpression expr = parser.expression();

        parser.tokens().consume(T_RIGHT_PAREN, "Expect ')' after expression.");

        return expr;
    }

    private ASTExpression parseMappingLiteral(Parser parser) {
        int line = parser.currLine();
        List<ASTExprMappingEntry> entries = new ArrayList<>();

        if (!parser.tokens().check(T_RIGHT_BRACKET)) {
            do {
                ASTExpression key = parser.expression();
                parser.tokens().consume(T_COLON, "Expect ':' after mapping key.");
                ASTExpression value = parser.expression();
                entries.add(new ASTExprMappingEntry(key, value));
            } while (parser.tokens().match(T_COMMA));
        }

        parser.tokens().consume(T_RIGHT_BRACKET, "Expect ']' after mapping literal.");
        return new ASTExprMappingLiteral(line, entries);
    }
}
