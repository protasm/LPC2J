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
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
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
    private ASTObject currObj;
    private Locals locals;

    public TokenList tokens() {
	return tokens;
    }

    public ASTObject currObj() {
	return currObj;
    }

    public Locals locals() {
	return locals;
    }

    public int currLine() {
	return tokens.current().line();
    }

    public ASTObject parse(String objName, TokenList tokens) {
	this.tokens = tokens;

	currObj = new ASTObject(0, inherit(), objName);

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

	locals = new Locals();

	ASTParamList parameters = parameters();

	tokens.consume(T_LEFT_BRACE, "Expected '{' after method declaration.");

	ASTStmtBlock body = block();
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

	    ASTParameter param = new ASTParameter(line, typeToken, nameToken);
	    Local local = new Local(typeToken.literal(), nameToken.lexeme());

	    parameters.add(param);
	    locals.add(local, true);
	} while (tokens.match(T_COMMA));

	tokens.consume(T_RIGHT_PAREN, "Expected ')' after parameter list.");

	return parameters;
    }

    private ASTStmtBlock block() {
	int line = currLine();

	locals.beginScope();

	List<ASTStatement> statements = new ArrayList<>();

	while (!tokens.check(T_RIGHT_BRACE) && !tokens.isAtEnd())
	    if (tokens.match(T_TYPE)) { // local declaration
		Local local = localDeclaration();

		locals.add(local, true); // sets slot # and depth

		if (tokens.match(T_EQUAL)) { //local assignment 
		    ASTExpression initializer = expression();

		    ASTExprLocalStore expr = new ASTExprLocalStore(line, local, initializer);
		    ASTStmtExpression exprStmt = new ASTStmtExpression(line, expr);

		    statements.add(exprStmt);
		}

		tokens.consume(T_SEMICOLON, "Expect ';' after local variable declaration.");
	    } else
		statements.add(statement());

	tokens.consume(T_RIGHT_BRACE, "Expected '}' after method body.");

	locals.endScope();

	return new ASTStmtBlock(line, statements);
    }

    @SuppressWarnings("unchecked")
    private Local localDeclaration() {
	Token<LPCType> typeToken = (Token<LPCType>) tokens.previous();
	Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expected local variable name.");

	String name = nameToken.lexeme();

	if (locals.hasCollision(name))
	    throw new ParseException("Already a local named '" + name + "' in current scope.");

	LPCType lpcType = typeToken.literal();
	
	return new Local(lpcType, name);
    }

    public ASTStatement statement() {
	if (tokens.match(T_RETURN))
	    return returnStatement();
	else if (tokens.match(T_LEFT_BRACE))
	    return block();
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

    private ASTStmtExpression expressionStatement() {
	int line = currLine();
	ASTExpression expr = expression();

	tokens.consume(T_SEMICOLON, "Expect ';' after expression.");

	return new ASTStmtExpression(line, expr);
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

    public static void main(String[] args) throws IOException {
	if (args.length != 1) {
	    System.err.println("Usage: java Parser <source-file>");

	    System.exit(1);
	}

	SourceFile sf = new SourceFile("/Users/jonathan/brainjar", args[0]);
	Scanner scanner = new Scanner();
	TokenList tokens = scanner.scan(sf.source());
	Parser parser = new Parser();

	ASTObject ast = parser.parse(sf.slashName(), tokens);

	System.out.println(ast);
    }
}
