package io.github.protasm.lpc2j.parser;

import static io.github.protasm.lpc2j.parser.Parser.Precedence.*;
import static io.github.protasm.lpc2j.scanner.TokenType.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.protasm.lpc2j.parser.ast.*;
import io.github.protasm.lpc2j.parser.ast.expr.*;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpressionStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.SourceFile;
import io.github.protasm.lpc2j.parser.parselet.*;
import io.github.protasm.lpc2j.scanner.Token;
import io.github.protasm.lpc2j.scanner.TokenList;
import io.github.protasm.lpc2j.scanner.*;

public class Parser {
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

    private TokenList tokens;
//    private boolean hadError;
//    private boolean panicMode;

    private ASTObject currObj;

    public Parser() {
//	hadError = false;
//	panicMode = false;
    }

    public TokenList tokens() {
	return tokens;
    }

    public int currLine() {
	return tokens.current().line();
    }

    public ASTObject currObj() {
	return currObj;
    }

    public ASTObject parse(String name, TokenList tokens) {
	this.tokens = tokens;

	String parentName = inherit();
	currObj = new ASTObject(0, parentName, name);

	while (!tokens.isAtEnd())
	    property();

	return currObj;
    }

    private String inherit() {
	if (tokens.match(T_INHERIT)) {
	    Token<String> parentToken = tokens.consume(T_STRING_LITERAL, "Expected string after 'inherit'.");

	    tokens.consume(T_SEMICOLON, "Expected ';' after inherited object path.");

	    return parentToken.literal();
	}

	return null;
    }

    private void property() {
	Token<LPCType> typeToken = tokens.consume(T_TYPE, "Expected property type.");
	Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expected property name.");

	if (tokens.match(T_LEFT_PAREN))
	    method(typeToken, nameToken);
	else
	    field(typeToken, nameToken);
    }

    private void field(Token<LPCType> typeToken, Token<String> nameToken) {
	int line = currLine();
	ASTExpression initializer = null;

	if (tokens.match(T_EQUAL))
	    initializer = expression();

	tokens.consume(T_SEMICOLON, "Expected ';' after field declaration.");

	ASTField field = new ASTField(line, currObj.name(), typeToken, nameToken, initializer);

	currObj.fields().put(field.name(), field);
    }

    private void method(Token<LPCType> typeToken, Token<String> nameToken) {
	int line = currLine();
	ASTParamList parameters = parameters();

	tokens.consume(T_LEFT_BRACE, "Expected '{' after method declaration.");

	ASTStmtBlock body = block(currLine());
	ASTMethod method = new ASTMethod(line, currObj.name(), typeToken, nameToken, parameters, body);

	currObj.methods().add(method);
    }

    private ASTParamList parameters() {
	int line = currLine();
	ASTParamList parameters = new ASTParamList(line);

	if (tokens.match(T_RIGHT_PAREN)) // No parameters
	    return parameters;

	do {
	    Token<LPCType> typeToken = tokens.consume(T_TYPE, "Expected parameter type.");
	    Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expected parameter name.");

	    parameters.add(new ASTParameter(line, typeToken, nameToken));
	} while (tokens.match(T_COMMA));

	tokens.consume(T_RIGHT_PAREN, "Expected ')' after parameter list.");

	return parameters;
    }

    private ASTStmtBlock block(int line) {
	List<ASTStatement> statements = new ArrayList<>();

	while (!tokens.check(T_RIGHT_BRACE) && !tokens.isAtEnd())
	    statements.add(statement());

	tokens.consume(T_RIGHT_BRACE, "Expected '}' after method body.");

	return new ASTStmtBlock(line, statements);
    }

    public ASTStatement statement() {
	if (tokens.match(T_RETURN))
	    return returnStatement();
	else
	    return expressionStatement();
    }

    private ASTStmtReturn returnStatement() {
	int line = currLine();

	if (tokens.match(T_SEMICOLON))
	    return new ASTStmtReturn(line, null);

	ASTExpression expr = expression();

	tokens.consume(T_SEMICOLON, "Expected ';' after return statement.");

	return new ASTStmtReturn(line, expr);
    }

    private ASTStmtExpressionStatement expressionStatement() {
	int line = currLine();
	ASTExpression expr = expression();

	tokens.consume(T_SEMICOLON, "Expect ';' after expression.");

	return new ASTStmtExpressionStatement(line, expr);
    }

    public ASTExpression expression() {
	return parsePrecedence(PREC_ASSIGNMENT);
    }

    public ASTExpression parsePrecedence(int precedence) {
	tokens.advance();

	PrefixParselet pParselet = PrattParser.getRule(tokens.previous()).prefix();

	if (pParselet == null)
	    throw new ParseException("Expect expression.", tokens.current());

	boolean canAssign = (precedence <= PREC_ASSIGNMENT);

	ASTExpression expr = pParselet.parse(this, canAssign);

	while (precedence <= PrattParser.getRule(tokens.current()).precedence()) {
	    tokens.advance();

	    InfixParselet iParselet = PrattParser.getRule(tokens.previous()).infix();

	    if (iParselet == null)
		throw new ParseException("Expect expression.", tokens.current());

	    expr = iParselet.parse(this, expr, canAssign);
	}

	if (canAssign)
	    if (tokens.match(T_EQUAL) || tokens.match(T_PLUS_EQUAL))
		throw new ParseException("Invalid assignment target.", tokens.current());

	return expr;
    }

//    public void transformCurrent(TokenType type) {
//	current = new Token(type, null, null, current.line());
//    }
//
//    public boolean hadError() {
//	return hadError;
//    }
//
//    public boolean panicMode() {
//	return panicMode;
//    }
//
//    public void synchronize() {
//	panicMode = false;
//
//	while (tokens.current().tType() != T_EOF) {
//	    if (tokens.previous().tType() == T_SEMICOLON)
//		return;
//
//	    switch (tokens.current().tType()) {
//	    case T_TYPE:
//	    case T_FOR:
//	    case T_IF:
//	    case T_WHILE:
//	    case T_RETURN:
//		return;
//
//	    default:
//		break;
//	    }
//
//	    tokens.advance();
//	}
//    }

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
//    public void errorAtCurrent(String message) {
//	errorAt(tokens.current(), message);
//    }
//
//    public void error(String message) {
//	errorAt(tokens.previous(), message);
//    }
//
//    private void errorAt(Token<?> token, String message) {
//	if (panicMode)
//	    return;
//
//	panicMode = true;
//
//	System.err.print("[line " + token.line() + "] Error");
//
//	if (token.tType() == T_EOF)
//	    System.err.print(" at end");
//	else if (token.tType() == T_ERROR) {
    ////TODO
//	} else
//	    System.err.print(" at '" + token.lexeme() + "'");
//
//	System.err.print(": " + message + "\n");
//
//	hadError = true;
//    }

//    @SuppressWarnings("unchecked")
//    public <T> Token<T> parseType(String errorMessage) {
//	tokens.consume(T_TYPE, errorMessage);
//
//	return (Token<T>) tokens.previous();
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T> Token<T> parseVariable(String errorMessage) {
//	tokens.consume(T_IDENTIFIER, errorMessage);
//
    ////		if (compiler.mb() != null && compiler.mb().workingScopeDepth() > 0) {
////			compiler.declareLocalVar(token);
////		}
//
//	return (Token<T>) tokens.previous();
//    }

    public static void main(String[] args) throws IOException {
	if (args.length != 1) {
	    System.err.println("Usage: java Parser <source-file>");

	    System.exit(1);
	}

	SourceFile sf = new SourceFile("/Users/jonathan/brainjar", args[0]);
	Scanner scanner = new Scanner();
	TokenList tokens = scanner.scan(sf.source());

	Parser parser = new Parser();
	ASTObject ast = parser.parse(sf.slashName(), tokens); // TODO

	System.out.println(ast);
    }
}
