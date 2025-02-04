package io.github.protasm.lpc2j.parser;

import static io.github.protasm.lpc2j.scanner.TokenType.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.protasm.lpc2j.parser.ast.*;
import io.github.protasm.lpc2j.parser.ast.expr.*;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.fs.FSSourceFile;
import io.github.protasm.lpc2j.fs.FSSourceFile;
import io.github.protasm.lpc2j.parser.parselet.*;
import io.github.protasm.lpc2j.scanner.Token;
import io.github.protasm.lpc2j.scanner.Tokens;
import io.github.protasm.lpc2j.scanner.*;

public class Parser {
    private Tokens tokens;
    private ASTObject currObj;
    private Locals locals;

    public Tokens tokens() {
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

    public ASTObject parse(String objName, Tokens tokens) {
	if (tokens == null)
	    return null;

	this.tokens = tokens;

	currObj = new ASTObject(0, objName);

	declarations(); // pass 1

	tokens.reset();

	definitions(); // pass 2

	return currObj;
    }

    private void declarations() {
	skipInherit();

	while (!tokens.isAtEnd())
	    property(false);
    }

    private void definitions() {
	currObj.setParentName(inherit());

	while (!tokens.isAtEnd())
	    property(true);
    }

    private String inherit() {
	if (!tokens.match(T_INHERIT))
	    return null;

	Token<String> parentToken = tokens.consume(T_STRING_LITERAL, "Expect string after 'inherit'.");

	tokens.consume(T_SEMICOLON, "Expect ';' after inherited object path.");

	return parentToken.lexeme();
    }

    private void property(boolean define) {
	Token<LPCType> typeToken = tokens.consume(T_TYPE, "Expect property type.");
	Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect property name.");
	LPCType lpcType = typeToken.literal();
	String name = nameToken.lexeme();

	if (tokens.match(T_LEFT_PAREN))
	    method(lpcType, name, define);
	else
	    field(lpcType, name, define);
    }

    private void field(LPCType lpcType, String name, boolean define) {
	if (!define) {
	    skipFieldInit();

	    ASTField field = new ASTField(currLine(), currObj.name(), lpcType, name);

	    currObj.addField(field);

	    return;
	}

	if (tokens.match(T_EQUAL)) {
	    ASTField field = currObj.fields().get(name);
	    ASTExpression initializer = expression();

	    field.setInitializer(initializer);
	}

	tokens.consume(T_SEMICOLON, "Expect ';' after field declaration.");
    }

    private void method(LPCType lpcType, String name, boolean define) {
	if (!define) {
	    skipMethodBody();

	    ASTMethod method = new ASTMethod(currLine(), currObj.name(), lpcType, name);

	    currObj.addMethod(method);

	    return;
	}

	ASTMethod method = currObj.methods().get(name);

	locals = new Locals();

	method.setParameters(parameters());

	tokens.consume(T_LEFT_BRACE, "Expect '{' after method declaration.");

	method.setBody(block());
    }

    private void skipInherit() {
	if (!tokens.match(T_INHERIT))
	    return;

	tokens.advanceThrough(T_SEMICOLON);
    }

    private void skipFieldInit() {
	tokens.advanceThrough(T_SEMICOLON);
    }

    private void skipMethodBody() {
	int count = 0;

	while (!tokens.isAtEnd())
	    if (tokens.match(T_LEFT_BRACE))
		count++;
	    else if (tokens.match(T_RIGHT_BRACE)) {
		count--;

		if (count == 0)
		    return;
	    } else
		tokens.advance();

	throw new ParseException("Unmatched '{' in method body.");
    }

    private ASTParameters parameters() {
	ASTParameters params = new ASTParameters(currLine());

	if (tokens.match(T_RIGHT_PAREN)) // No parameters
	    return params;

	do {
	    Token<LPCType> typeToken = tokens.consume(T_TYPE, "Expect parameter type.");
	    Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect parameter name.");
	    LPCType lpcType = typeToken.literal();
	    String name = nameToken.lexeme();

	    ASTParameter param = new ASTParameter(currLine(), lpcType, name);
	    Local local = new Local(lpcType, name);

	    params.add(param);

	    locals.add(local, true);
	} while (tokens.match(T_COMMA));

	tokens.consume(T_RIGHT_PAREN, "Expect ')' after method parameters.");

	return params;
    }

    public ASTArguments arguments() {
	ASTArguments args = new ASTArguments(currLine());

	tokens.consume(T_LEFT_PAREN, "Expect '(' after method name.");

	if (tokens.match(T_RIGHT_PAREN)) // No arguments
	    return args;

	do {
	    ASTExpression expr = expression();
	    ASTArgument arg = new ASTArgument(currLine(), expr);

	    args.add(arg);
	} while (tokens.match(T_COMMA));

	tokens.consume(T_RIGHT_PAREN, "Expect ')' after method arguments.");

	return args;
    }

    private ASTStmtBlock block() {
	locals.beginScope();

	List<ASTStatement> statements = new ArrayList<>();

	while (!tokens.check(T_RIGHT_BRACE) && !tokens.isAtEnd())
	    if (tokens.match(T_TYPE)) { // local declaration
		Local local = local();

		locals.add(local, true); // sets slot # and depth

		if (tokens.match(T_EQUAL)) { // local assignment
		    ASTExprLocalStore expr = new ASTExprLocalStore(currLine(), local, expression());
		    ASTStmtExpression exprStmt = new ASTStmtExpression(currLine(), expr);

		    statements.add(exprStmt);
		}

		tokens.consume(T_SEMICOLON, "Expect ';' after local variable declaration.");
	    } else
		statements.add(statement());

	tokens.consume(T_RIGHT_BRACE, "Expect '}' after method body.");

	locals.endScope();

	return new ASTStmtBlock(currLine(), statements);
    }

    private Local local() {
	Token<LPCType> typeToken = tokens.previous();
	Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect local variable name.");
	LPCType lpcType = typeToken.literal();
	String name = nameToken.lexeme();

	if (locals.hasCollision(name))
	    throw new ParseException("Already a local variable named '" + name + "' in current scope.");

	return new Local(lpcType, name);
    }

    public ASTStatement statement() {
	if (tokens.match(T_IF))
	    return ifStatement();
	else if (tokens.match(T_RETURN))
	    return returnStatement();
	else if (tokens.match(T_LEFT_BRACE))
	    return block();
	else
	    return expressionStatement();
    }

    private ASTStatement ifStatement() {
	ASTExpression expr = ifCondition();
	ASTStatement stmtThen = statement();

	if (tokens.match(T_ELSE))
	    return new ASTStmtIfThenElse(currLine(), expr, stmtThen, statement());
	else
	    return new ASTStmtIfThenElse(currLine(), expr, stmtThen, null);
    }

    private ASTExpression ifCondition() {
	tokens.consume(T_LEFT_PAREN, "Expect '(' after if.");

	ASTExpression expr = expression();

	tokens.consume(T_RIGHT_PAREN, "Expect ')' after if condition.");

	return expr;
    }

    private ASTStmtReturn returnStatement() {
	if (tokens.match(T_SEMICOLON))
	    return new ASTStmtReturn(currLine(), null);

	ASTExpression expr = expression();

	tokens.consume(T_SEMICOLON, "Expect ';' after return statement.");

	return new ASTStmtReturn(currLine(), expr);
    }

    private ASTStmtExpression expressionStatement() {
	ASTExpression expr = expression();

	tokens.consume(T_SEMICOLON, "Expect ';' after expression.");

	return new ASTStmtExpression(currLine(), expr);
    }

    public ASTExpression expression() {
	return parsePrecedence(PrattParser.Precedence.PREC_ASSIGNMENT);
    }

    public ASTExpression parsePrecedence(int precedence) {
	tokens.advance();

	PrefixParselet pp = PrattParser.getRule(tokens.previous()).prefix();

	if (pp == null)
	    throw new ParseException("Expect expression.", tokens.current());

	boolean canAssign = (precedence <= PrattParser.Precedence.PREC_ASSIGNMENT);

	ASTExpression expr = pp.parse(this, canAssign);

	while (precedence <= PrattParser.getRule(tokens.current()).precedence()) {
	    tokens.advance();

	    InfixParselet ip = PrattParser.getRule(tokens.previous()).infix();

	    if (ip == null)
		throw new ParseException("Expect expression.", tokens.current());

	    expr = ip.parse(this, expr, canAssign);
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

	FSSourceFile sf = new FSSourceFile("/Users/jonathan/brainjar", args[0]);
	Scanner scanner = new Scanner();
	Tokens tokens = scanner.scan(sf.source());
	Parser parser = new Parser();

	ASTObject ast = parser.parse(sf.slashName(), tokens);

	System.out.println(ast);
    }
}
