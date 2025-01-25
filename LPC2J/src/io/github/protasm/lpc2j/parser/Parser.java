package io.github.protasm.lpc2j.parser;

import static io.github.protasm.lpc2j.parser.Parser.Precedence.*;
import static io.github.protasm.lpc2j.scanner.TokenType.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.protasm.lpc2j.parser.ast.*;
import io.github.protasm.lpc2j.parser.ast.expr.*;
import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.parselet.*;
import io.github.protasm.lpc2j.scanner.Token;
import io.github.protasm.lpc2j.scanner.TokenType;
import io.github.protasm.lpc2j.parser.*;
import io.github.protasm.lpc2j.scanner.*;

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

    private final String name;
    private final TokenList tokens;
    private Map<TokenType, ParseRule> tokenTypeToRule;
    private boolean hadError;
    private boolean panicMode;

    public Parser(String name, TokenList tokens) {
	this.name = name;
	this.tokens = tokens;

	tokenTypeToRule = new HashMap<>();

	registerTokenTypesToRules();

	hadError = false;
	panicMode = false;
    }

    public ASTObject parse() {
	ASTObject object = new ASTObject(inherit(), name);

	while (!tokens.isAtEnd())
	    property(object);

	return object;
    }

    private String inherit() {
	if (tokens.match(T_INHERIT)) {
	    Token<?> parentToken = tokens.consume(T_STRING_LITERAL, "Expected string after 'inherit'.");
	    tokens.consume(T_SEMICOLON, "Expected ';' after inherited object path.");

	    return (String) parentToken.literal();
	}

	return null;
    }

    private void property(ASTObject object) {
	Token<LPCType> typeToken = tokens.consume(T_TYPE, LPCType.class, "Expected property type.");
	Token<String> nameToken = tokens.consume(T_IDENTIFIER, String.class, "Expected property name.");

	if (tokens.match(T_LEFT_PAREN))
	    method(object, typeToken, nameToken);
	else
	    field(object, typeToken, nameToken);
    }

    private void field(ASTObject object, Token<LPCType> typeToken, Token<String> nameToken) {
	ASTExpression initializer = null;

	if (tokens.match(T_EQUAL))
	    initializer = expression(tokens.current());

	tokens.consume(T_SEMICOLON, "Expected ';' after field declaration.");

	ASTField field = new ASTField(typeToken, nameToken, initializer);

	object.fields().add(field);
    }

    private void method(ASTObject object, Token typeToken, Token nameToken) {
	ASTParameters parameters = parameters();

	tokens.consume(T_LEFT_BRACE, "Expected '{' after method declaration.");

	ASTStmtBlock body = block(tokens.current());
	ASTMethod method = new ASTMethod(typeToken, nameToken, parameters, body);

	object.methods().add(method);
    }

    private ASTParameters parameters() {
	ASTParameters parameters = new ASTParameters(tokens.current());

	if (tokens.match(T_RIGHT_PAREN)) // No parameters
	    return parameters;

	do {
	    Token typeToken = tokens.consume(new TokenType[] { T_INT, T_STRING, T_OBJECT }, "Expected parameter type.");
	    Token nameToken = tokens.consume(T_IDENTIFIER, "Expected parameter name.");

	    parameters.add(new ASTParameter(typeToken, nameToken));
	} while (tokens.match(T_COMMA));

	tokens.consume(T_RIGHT_PAREN, "Expected ')' after parameter list.");

	return parameters;
    }

    private ASTStmtBlock block(Token token) {
	List<ASTStatement> statements = new ArrayList<>();

	while (!tokens.check(T_RIGHT_BRACE) && !tokens.isAtEnd())
	    statements.add(statement());

	tokens.consume(T_RIGHT_BRACE, "Expected '}' after method body.");

	return new ASTStmtBlock(token, statements);
    }

    public ASTStatement statement() {
	if (tokens.match(T_RETURN))
	    return returnStatement(tokens.current());
	else
	    return expressionStatement(tokens.current());
    }

    private ASTStmtReturn returnStatement(Token startToken) {
	if (tokens.match(T_SEMICOLON))
	    return new ASTStmtReturn(startToken, null);

	ASTExpression expr = expression(tokens.current());

	tokens.consume(T_SEMICOLON, "Expected ';' after return statement.");

	return new ASTStmtReturn(startToken, expr);
    }

    private ASTStmtExpressionStatement expressionStatement(Token startToken) {
	return null;
    }

    private ASTExpression expression(Token startToken) {
	// For simplicity, just return an expression node. In a complete parser, this
	// would
	// involve recursive calls for handling operators, parentheses, and other
	// expressions.
	ASTExpression expr = primaryExpression(); // This is just a placeholder for real parsing logic

	return expr;
    }

    private ASTExpression primaryExpression() {
	if (tokens.match(T_IDENTIFIER))
	    return new ASTExprVariable(tokens.previous());
	else if (tokens.match(T_INTEGER_LITERAL))
	    return new ASTExprIntegerLiteral(tokens.previous());
	else if (tokens.match(T_STRING_LITERAL))
	    return new ASTExprStringLiteral(tokens.previous());

	throw new ParseException("Unexpected token for primary expression.", tokens.current());
    }

//    public void transformCurrent(TokenType type) {
//	current = new Token(type, null, null, current.line());
//    }

    public boolean hadError() {
	return hadError;
    }

    public boolean panicMode() {
	return panicMode;
    }

    public void synchronize() {
	panicMode = false;

	while (tokens.current().type() != T_EOF) {
	    if (tokens.previous().type() == T_SEMICOLON) {
		return;
	    }

	    switch (tokens.current().type()) {
	    case T_TYPE:
	    case T_FOR:
	    case T_IF:
	    case T_WHILE:
	    case T_RETURN:
		return;

	    default:
		break;
	    }

	    advance();
	}
    }

//    public void advance() {
//	if (!tokensItr.hasNext()) {
//	    return;
//	}
//
//	previous = current;
//
//	for (;;) {
//	    current = tokensItr.next();
//
//	    if (current.type() != T_ERROR) {
//		break;
//	    }
//
//	    errorAtCurrent(current.lexeme());
//	}
//    }
//    
    public void errorAtCurrent(String message) {
	errorAt(tokens.current(), message);
    }

    public void error(String message) {
	errorAt(tokens.previous(), message);
    }

    private void errorAt(Token<?> token, String message) {
	if (panicMode) {
	    return;
	}

	panicMode = true;

	System.err.print("[line " + token.line() + "] Error");

	if (token.type() == T_EOF) {
	    System.err.print(" at end");
	} else if (token.type() == T_ERROR) {

	} else {
	    System.err.print(" at '" + token.lexeme() + "'");
	}

	System.err.print(": " + message + "\n");

	hadError = true;
    }

    public void parsePrecedence(int precedence, boolean inBinaryOp) {
	tokens.advance();

	Parselet prefixParselet = getRule(tokens.previous().type()).prefix();

	if (prefixParselet == null) {
	    error("Expect expression.");

	    return;
	}

	boolean canAssign = (precedence <= PREC_ASSIGNMENT);

	prefixParselet.parse(this, canAssign, inBinaryOp);

	while (precedence <= getRule(tokens.current().type()).precedence()) {
	    tokens.advance();

	    Parselet infixParselet = getRule(tokens.previous().type()).infix();

	    infixParselet.parse(this, canAssign, false);
	}

	if (canAssign)
	    if (tokens.match(T_EQUAL) || tokens.match(T_PLUS_EQUAL))
		error("Invalid assignment target.");
    }

    public Token parseType(String errorMessage) {
	tokens.consume(T_TYPE, errorMessage);

	return tokens.previous();
    }

    public Token parseVariable(String errorMessage) {
	consume(T_IDENTIFIER, errorMessage);

//		if (compiler.mb() != null && compiler.mb().workingScopeDepth() > 0) {
//			compiler.declareLocalVar(token);
//		}

	return tokens.previous();
    }

    public static void main(String[] args) {
	if (args.length != 1) {
	    System.err.println("Usage: java Parser <source-file>");

	    System.exit(1);
	}

	String fileName = args[0];
	Path filePath = Paths.get(fileName);
	String source;

	try {
	    source = Files.readString(filePath);

	    Scanner scanner = new Scanner(source);
	    TokenList tokens = scanner.scan();

	    Parser parser = new Parser(fileName, tokens);
	    ASTObject ast = parser.parse();

	    System.out.println(ast);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    private void register(TokenType type, Parselet prefix, Parselet infix, int precedence) {
	tokenTypeToRule.put(type, new ParseRule(prefix, infix, precedence));
    }

    public ParseRule getRule(TokenType type) {
	return tokenTypeToRule.get(type);
    }

    private void registerTokenTypesToRules() {
	// Token Type, Prefix Parselet, Infix Parselet, precedence
	register(T_MINUS, new UnaryParselet(), new BinaryParselet(), PREC_TERM);

	register(T_PLUS, null, new BinaryParselet(), PREC_TERM);
	register(T_STAR, null, new BinaryParselet(), PREC_FACTOR);
	register(T_SLASH, null, new BinaryParselet(), PREC_FACTOR);

	register(T_INVOKE, null, new InvokeParselet(), PREC_NONE);

	register(T_FALSE, new LiteralParselet(), null, PREC_NONE);
	register(T_IDENTIFIER, new IdentifierParselet(), null, PREC_NONE);
	register(T_INT_LITERAL, new NumberParselet(), null, PREC_NONE);
	register(T_FLOAT_LITERAL, new NumberParselet(), null, PREC_NONE);
	register(T_STRING, new StringParselet(), null, PREC_NONE);
	register(T_TRUE, new LiteralParselet(), null, PREC_NONE);

	register(T_COLON, null, null, PREC_NONE);
	register(T_COMMA, null, null, PREC_NONE);
	register(T_ELSE, null, null, PREC_NONE);
	register(T_EOF, null, null, PREC_NONE);
	register(T_EQUAL, null, null, PREC_NONE);
	register(T_ERROR, null, null, PREC_NONE);
	register(T_FOR, null, null, PREC_NONE);
	register(T_IF, null, null, PREC_NONE);
	register(T_INHERIT, null, null, PREC_NONE);
	register(T_LEFT_BRACE, null, null, PREC_NONE);
	register(T_MINUS_EQUAL, null, null, PREC_NONE);
	register(T_MINUS_MINUS, null, null, PREC_NONE);
	register(T_PLUS_EQUAL, null, null, PREC_NONE);
	register(T_PLUS_PLUS, null, null, PREC_NONE);
	register(T_RETURN, null, null, PREC_NONE);
	register(T_RIGHT_BRACE, null, null, PREC_NONE);
	register(T_RIGHT_BRACKET, null, null, PREC_NONE);
	register(T_RIGHT_PAREN, null, null, PREC_NONE);
	register(T_SEMICOLON, null, null, PREC_NONE);
	register(T_SLASH_EQUAL, null, null, PREC_NONE);
	register(T_STAR_EQUAL, null, null, PREC_NONE);
	register(T_TYPE, null, null, PREC_NONE);
	register(T_WHILE, null, null, PREC_NONE);
    }
}
