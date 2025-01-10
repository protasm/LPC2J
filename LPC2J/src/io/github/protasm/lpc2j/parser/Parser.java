package io.github.protasm.lpc2j.parser;

import static io.github.protasm.lpc2j.parser.Parser.Precedence.PREC_ASSIGNMENT;
import static io.github.protasm.lpc2j.parser.Parser.Precedence.PREC_FACTOR;
import static io.github.protasm.lpc2j.parser.Parser.Precedence.PREC_NONE;
import static io.github.protasm.lpc2j.parser.Parser.Precedence.PREC_TERM;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_COLON;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_COMMA;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_ELSE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_EOF;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_ERROR;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_FALSE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_FOR;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_IDENTIFIER;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_IF;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_INHERIT;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_INVOKE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LEFT_BRACE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_MINUS;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_MINUS_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_MINUS_MINUS;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_NUM_FLOAT;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_NUM_INT;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_PLUS;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_PLUS_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_PLUS_PLUS;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RETURN;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RIGHT_BRACE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RIGHT_BRACKET;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RIGHT_PAREN;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_SEMICOLON;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_SLASH;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_SLASH_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_STAR;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_STAR_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_STRING;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_TRUE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_TYPE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_WHILE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.parselet.BinaryParselet;
import io.github.protasm.lpc2j.parser.parselet.CallParselet;
import io.github.protasm.lpc2j.parser.parselet.LiteralParselet;
import io.github.protasm.lpc2j.parser.parselet.NumberParselet;
import io.github.protasm.lpc2j.parser.parselet.Parselet;
import io.github.protasm.lpc2j.parser.parselet.StringParselet;
import io.github.protasm.lpc2j.parser.parselet.UnaryParselet;
import io.github.protasm.lpc2j.parser.parselet.VariableParselet;
import io.github.protasm.lpc2j.scanner.Token;
import io.github.protasm.lpc2j.scanner.TokenType;

public class Parser {
    public static final class Precedence {
	public static final int PREC_NONE = 0;
	public static final int PREC_ASSIGNMENT = 1;
	public static final int PREC_OR = 2;
	public static final int PREC_AND = 3;
	public static final int PREC_EQUALITY = 4;
	public static final int PREC_COMPARISON = 5;
	public static final int PREC_TERM = 6;
	public static final int PREC_FACTOR = 7;
	public static final int PREC_UNARY = 8;
	public static final int PREC_INDEX = 9;
	public static final int PREC_CALL = 10;
	public static final int PREC_PRIMARY = 11;

	private Precedence() {
	}
    }

    private LPC2J compiler;

    private Iterator<Token> tokensItr;
    private Map<TokenType, ParseRule> tokenTypeToRule;

    private Token previous;
    private Token current;
    private boolean hadError;
    private boolean panicMode;

    public Parser(LPC2J compiler, List<Token> tokens) {
	this.compiler = compiler;

	tokensItr = tokens.listIterator();
	tokenTypeToRule = new HashMap<>();

	registerTokenTypesToRules();

	previous = null;
	current = null;
	hadError = false;
	panicMode = false;
    }

    public Token previous() {
	return previous;
    }

    public Token current() {
	return current;
    }

    public void setPrevious(Token token) {
	previous = token;
    }

    public void setCurrent(Token token) {
	current = token;
    }

    public void transformCurrent(TokenType type) {
	current = new Token(type, null, null, current.line());
    }

    public boolean hadError() {
	return hadError;
    }

    public boolean panicMode() {
	return panicMode;
    }

    public void synchronize() {
	panicMode = false;

	while (current.type() != TOKEN_EOF) {
	    if (previous.type() == TOKEN_SEMICOLON) {
		return;
	    }

	    switch (current.type()) {
	    case TOKEN_TYPE:
	    case TOKEN_FOR:
	    case TOKEN_IF:
	    case TOKEN_WHILE:
	    case TOKEN_RETURN:
		return;

	    default:
		break;
	    }

	    advance();
	}
    }

    public void advance() {
	if (!tokensItr.hasNext()) {
	    return;
	}

	previous = current;

	for (;;) {
	    current = tokensItr.next();

	    if (current.type() != TOKEN_ERROR) {
		break;
	    }

	    errorAtCurrent(current.lexeme());
	}
    }

    public List<Token> collectUntil(List<TokenType> types) {
	List<Token> tokens = new ArrayList<>();

	while (current().type() != TOKEN_EOF && !types.contains(current().type())) {
	    advance();

	    tokens.add(previous());
	}

	return tokens;
    }

    public boolean match(TokenType type) {
	if (!check(type)) {
	    return false;
	}

	advance();

	return true;
    }

    public boolean check(TokenType type) {
	return current.type() == type;
    }

    public void consume(TokenType type, String message) {
	if (current.type() == type) {
	    advance();

	    return;
	}

	errorAtCurrent(message);
    }

    public void errorAtCurrent(String message) {
	errorAt(current, message);
    }

    public void error(String message) {
	errorAt(previous, message);
    }

    private void errorAt(Token token, String message) {
	if (panicMode) {
	    return;
	}

	panicMode = true;

	System.err.print("[line " + token.line() + "] Error");

	if (token.type() == TOKEN_EOF) {
	    System.err.print(" at end");
	} else if (token.type() == TOKEN_ERROR) {

	} else {
	    System.err.print(" at '" + token.lexeme() + "'");
	}

	System.err.print(": " + message + "\n");

	hadError = true;
    }

    public void parsePrecedence(int precedence, boolean inBinaryOp) {
	advance();

	Parselet prefixParselet = getRule(previous.type()).prefix();

	if (prefixParselet == null) {
	    error("Expect expression.");

	    return;
	}

	boolean canAssign = (precedence <= PREC_ASSIGNMENT);

	prefixParselet.parse(this, compiler, canAssign, inBinaryOp);

	while (precedence <= getRule(current.type()).precedence()) {
	    advance();

	    Parselet infixParselet = getRule(previous.type()).infix();

	    infixParselet.parse(this, compiler, canAssign, false);
	}

	if (canAssign) {
	    if (match(TOKEN_EQUAL) || match(TOKEN_PLUS_EQUAL)) {

		error("Invalid assignment target.");
	    }
	}
    }

    public Token parseType(String errorMessage) {
	consume(TOKEN_TYPE, errorMessage);

	return previous();
    }

    public Token parseVariable(String errorMessage) {
	consume(TOKEN_IDENTIFIER, errorMessage);

//		if (compiler.mb() != null && compiler.mb().workingScopeDepth() > 0) {
//			compiler.declareLocalVar(token);
//		}

	return previous();
    }

    private void register(TokenType type, Parselet prefix, Parselet infix, int precedence) {
	tokenTypeToRule.put(type, new ParseRule(prefix, infix, precedence));
    }

    public ParseRule getRule(TokenType type) {
	return tokenTypeToRule.get(type);
    }

    private void registerTokenTypesToRules() {
	// Token Type, Prefix Parselet, Infix Parselet, precedence
	register(TOKEN_MINUS, new UnaryParselet(), new BinaryParselet(), PREC_TERM);

	register(TOKEN_PLUS, null, new BinaryParselet(), PREC_TERM);
	register(TOKEN_STAR, null, new BinaryParselet(), PREC_FACTOR);
	register(TOKEN_SLASH, null, new BinaryParselet(), PREC_FACTOR);

	register(TOKEN_INVOKE, null, new CallParselet(), PREC_NONE);

	register(TOKEN_FALSE, new LiteralParselet(), null, PREC_NONE);
	register(TOKEN_IDENTIFIER, new VariableParselet(), null, PREC_NONE);
	register(TOKEN_NUM_INT, new NumberParselet(), null, PREC_NONE);
	register(TOKEN_NUM_FLOAT, new NumberParselet(), null, PREC_NONE);
	register(TOKEN_STRING, new StringParselet(), null, PREC_NONE);
	register(TOKEN_TRUE, new LiteralParselet(), null, PREC_NONE);

	register(TOKEN_COLON, null, null, PREC_NONE);
	register(TOKEN_COMMA, null, null, PREC_NONE);
	register(TOKEN_ELSE, null, null, PREC_NONE);
	register(TOKEN_EOF, null, null, PREC_NONE);
	register(TOKEN_EQUAL, null, null, PREC_NONE);
	register(TOKEN_ERROR, null, null, PREC_NONE);
	register(TOKEN_FOR, null, null, PREC_NONE);
	register(TOKEN_IF, null, null, PREC_NONE);
	register(TOKEN_INHERIT, null, null, PREC_NONE);
	register(TOKEN_LEFT_BRACE, null, null, PREC_NONE);
	register(TOKEN_MINUS_EQUAL, null, null, PREC_NONE);
	register(TOKEN_MINUS_MINUS, null, null, PREC_NONE);
	register(TOKEN_PLUS_EQUAL, null, null, PREC_NONE);
	register(TOKEN_PLUS_PLUS, null, null, PREC_NONE);
	register(TOKEN_RETURN, null, null, PREC_NONE);
	register(TOKEN_RIGHT_BRACE, null, null, PREC_NONE);
	register(TOKEN_RIGHT_BRACKET, null, null, PREC_NONE);
	register(TOKEN_RIGHT_PAREN, null, null, PREC_NONE);
	register(TOKEN_SEMICOLON, null, null, PREC_NONE);
	register(TOKEN_SLASH_EQUAL, null, null, PREC_NONE);
	register(TOKEN_STAR_EQUAL, null, null, PREC_NONE);
	register(TOKEN_TYPE, null, null, PREC_NONE);
	register(TOKEN_WHILE, null, null, PREC_NONE);
    }
}
