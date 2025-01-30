package io.github.protasm.lpc2j.parser;

import java.util.HashMap;
import java.util.Map;

import io.github.protasm.lpc2j.parser.parselet.*;
import io.github.protasm.lpc2j.scanner.Token;
import io.github.protasm.lpc2j.scanner.TokenType;

import static io.github.protasm.lpc2j.parser.Parser.Precedence.*;
import static io.github.protasm.lpc2j.scanner.TokenType.*;

public class PrattParser {
	private static final Map<TokenType, ParseRule> tokenTypeToRule;

	static {
		tokenTypeToRule = new HashMap<>();

		tokenTypeToRule.put(T_MINUS, new ParseRule(new PrefixUnaryOp(), new InfixBinaryOp(), PREC_TERM));
		tokenTypeToRule.put(T_PLUS, new ParseRule(null, new InfixBinaryOp(), PREC_TERM));
		tokenTypeToRule.put(T_STAR, new ParseRule(null, new InfixBinaryOp(), PREC_FACTOR));
		tokenTypeToRule.put(T_SLASH, new ParseRule(null, new InfixBinaryOp(), PREC_FACTOR));
		tokenTypeToRule.put(T_LESS, new ParseRule(null, new InfixBinaryOp(), PREC_COMPARISON));

		tokenTypeToRule.put(T_RIGHT_ARROW, new ParseRule(null, new InfixInvoke(), PREC_NONE));

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
