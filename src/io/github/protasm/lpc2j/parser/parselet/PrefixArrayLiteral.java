package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.token.TokenType.T_COMMA;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_BRACE;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayLiteral;
import java.util.ArrayList;
import java.util.List;

public class PrefixArrayLiteral implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
        List<ASTExpression> elements = new ArrayList<>();
        int line = parser.currLine();

        if (!parser.tokens().check(T_RIGHT_BRACE)) {
            do {
                elements.add(parser.expression());
            } while (parser.tokens().match(T_COMMA));
        }

        parser.tokens().consume(T_RIGHT_BRACE, "Expect '}' after array literal.");

        return new ASTExprArrayLiteral(line, elements);
    }
}
