package io.github.protasm.lpc2j.parser;

import static io.github.protasm.lpc2j.parser.PrattParser.Precedence.PREC_AND;
import static io.github.protasm.lpc2j.parser.PrattParser.Precedence.PREC_COMPARISON;
import static io.github.protasm.lpc2j.parser.PrattParser.Precedence.PREC_EQUALITY;
import static io.github.protasm.lpc2j.parser.PrattParser.Precedence.PREC_FACTOR;
import static io.github.protasm.lpc2j.parser.PrattParser.Precedence.PREC_NONE;
import static io.github.protasm.lpc2j.parser.PrattParser.Precedence.PREC_OR;
import static io.github.protasm.lpc2j.parser.PrattParser.Precedence.PREC_TERM;
import static io.github.protasm.lpc2j.token.TokenType.T_BANG;
import static io.github.protasm.lpc2j.token.TokenType.T_BANG_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_DBL_AMP;
import static io.github.protasm.lpc2j.token.TokenType.T_DBL_PIPE;
import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_FALSE;
import static io.github.protasm.lpc2j.token.TokenType.T_FLOAT_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_GREATER;
import static io.github.protasm.lpc2j.token.TokenType.T_GREATER_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.token.TokenType.T_INT_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.lpc2j.token.TokenType.T_LESS;
import static io.github.protasm.lpc2j.token.TokenType.T_LESS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS;
import static io.github.protasm.lpc2j.token.TokenType.T_SLASH;
import static io.github.protasm.lpc2j.token.TokenType.T_STAR;
import static io.github.protasm.lpc2j.token.TokenType.T_STRING_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_TRUE;

import java.util.HashMap;
import java.util.Map;

import io.github.protasm.lpc2j.parser.parselet.InfixBinaryOp;
import io.github.protasm.lpc2j.parser.parselet.PrefixIdentifier;
import io.github.protasm.lpc2j.parser.parselet.PrefixLParen;
import io.github.protasm.lpc2j.parser.parselet.PrefixLiteral;
import io.github.protasm.lpc2j.parser.parselet.PrefixNumber;
import io.github.protasm.lpc2j.parser.parselet.PrefixString;
import io.github.protasm.lpc2j.parser.parselet.PrefixUnaryOp;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenType;

public class PrattParser {
    public static final class Precedence {
        public static final int PREC_NONE = 0;
        public static final int PREC_ASSIGNMENT = 1; // =
        public static final int PREC_OR = 2; // or
        public static final int PREC_AND = 3; // and
        public static final int PREC_EQUALITY = 4; // == !=
        public static final int PREC_COMPARISON = 5; // < > <= >=
        public static final int PREC_TERM = 6; // + -
        public static final int PREC_FACTOR = 7; // * /
        public static final int PREC_UNARY = 8; // ! -
        public static final int PREC_CALL = 9; // ()
        public static final int PREC_PRIMARY = 10;

        // Precedence()
        private Precedence() {
        }
    }

    private static final Map<TokenType, ParseRule> tokenTypeToRule;

    static {
        tokenTypeToRule = new HashMap<>();

        tokenTypeToRule.put(T_LEFT_PAREN, new ParseRule(new PrefixLParen(), null, PREC_NONE));

        tokenTypeToRule.put(T_BANG, new ParseRule(new PrefixUnaryOp(), null, PREC_NONE));

        tokenTypeToRule.put(T_MINUS, new ParseRule(new PrefixUnaryOp(), new InfixBinaryOp(), PREC_TERM));
        tokenTypeToRule.put(T_PLUS, new ParseRule(null, new InfixBinaryOp(), PREC_TERM));
        tokenTypeToRule.put(T_STAR, new ParseRule(null, new InfixBinaryOp(), PREC_FACTOR));
        tokenTypeToRule.put(T_SLASH, new ParseRule(null, new InfixBinaryOp(), PREC_FACTOR));

        tokenTypeToRule.put(T_DBL_PIPE, new ParseRule(null, new InfixBinaryOp(), PREC_OR));
        tokenTypeToRule.put(T_DBL_AMP, new ParseRule(null, new InfixBinaryOp(), PREC_AND));

        tokenTypeToRule.put(T_GREATER, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));
        tokenTypeToRule.put(T_GREATER_EQUAL, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));
        tokenTypeToRule.put(T_LESS, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));
        tokenTypeToRule.put(T_LESS_EQUAL, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));

        tokenTypeToRule.put(T_BANG_EQUAL, new ParseRule(null, new InfixBinaryOp(), PREC_EQUALITY));
        tokenTypeToRule.put(T_EQUAL_EQUAL, new ParseRule(null, new InfixBinaryOp(), PREC_EQUALITY));

        tokenTypeToRule.put(T_FALSE, new ParseRule(new PrefixLiteral(), null, PREC_NONE));
        tokenTypeToRule.put(T_IDENTIFIER, new ParseRule(new PrefixIdentifier(), null, PREC_NONE));
        tokenTypeToRule.put(T_INT_LITERAL, new ParseRule(new PrefixNumber(), null, PREC_NONE));
        tokenTypeToRule.put(T_FLOAT_LITERAL, new ParseRule(new PrefixNumber(), null, PREC_NONE));
        tokenTypeToRule.put(T_STRING_LITERAL, new ParseRule(new PrefixString(), null, PREC_NONE));
        tokenTypeToRule.put(T_TRUE, new ParseRule(new PrefixLiteral(), null, PREC_NONE));

    }

    private PrattParser() {
    }

    public static ParseRule getRule(Token<?> token) {
        ParseRule rule = tokenTypeToRule.get(token.type());

        return (rule != null ? rule : new ParseRule(null, null, PREC_NONE));
    }
}
