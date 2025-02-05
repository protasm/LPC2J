package io.github.protasm.lpc2j.parser;

import java.util.HashMap;
import java.util.Map;

import io.github.protasm.lpc2j.parser.parselet.*;
import io.github.protasm.lpc2j.scanner.Token;
import io.github.protasm.lpc2j.scanner.TokenType;

import static io.github.protasm.lpc2j.scanner.TokenType.*;
import static io.github.protasm.lpc2j.parser.PrattParser.Precedence.*;

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

		tokenTypeToRule.put(T_GREATER, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));
		tokenTypeToRule.put(T_LESS, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));

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
		ParseRule rule = tokenTypeToRule.get(token.tType());

		return (rule != null ? rule : new ParseRule(null, null, PREC_NONE));
	}
}
