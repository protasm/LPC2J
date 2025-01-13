package io.github.protasm.lpc2j;

import static io.github.protasm.lpc2j.InstrType.*;
import static io.github.protasm.lpc2j.JType.JOBJECT;
import static io.github.protasm.lpc2j.SymbolType.SYM_LOCAL;
import static io.github.protasm.lpc2j.parser.Parser.Precedence.PREC_ASSIGNMENT;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_COMMA;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_EOF;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_INVOKE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LEFT_BRACE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LEFT_PAREN;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RETURN;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RIGHT_BRACE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RIGHT_PAREN;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_SEMICOLON;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_TYPE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.scanner.Token;

public class LPC2J {
    private String sysIncludePath;
    private String quoteIncludePath;

    private ClassBuilder cb;

    private Parser parser;
    private List<FieldInitializer> fieldInitializers;

    public LPC2J(String sysIncludePath, String quoteIncludePath) {
	this.sysIncludePath = sysIncludePath;
	this.quoteIncludePath = quoteIncludePath;
    }

    public LPC2J() {
	this(".", ".");
    }

    public ClassBuilder cb() {
	return cb;
    }

    public Parser parser() {
	return parser;
    }

    public byte[] compile(SourceFile sourceFile) throws IOException {
	String fullClassName = "io/github/protasm/brainjar/lpc/" + sourceFile.className();
	cb = new ClassBuilder(fullClassName);

	Scanner scanner = new Scanner(sourceFile.source(), sysIncludePath, quoteIncludePath);
	parser = new Parser(this, scanner.scan());
	fieldInitializers = new ArrayList<>();

	parser.advance(); // to first token

	while (!parser.match(TOKEN_EOF))
	    memberDeclaration();

	constructor();

	cb.finish();

	return cb.bytes();
    }

    private void memberDeclaration() {
	Token typeToken = parser.parseType("Expect member type.");
	Token nameToken = parser.parseVariable("Expect member name.");

	if (parser.check(TOKEN_LEFT_PAREN))
	    methodDeclaration(typeToken, nameToken);
	else
	    fieldDeclaration(typeToken, nameToken);

	if (parser.panicMode()) {
	    parser.synchronize();
	}
    }

    private void fieldDeclaration(Token typeToken, Token nameToken) {
	String lpcType = typeToken.lexeme();
	JType jType = JType.jTypeForLPCType(lpcType);
	String name = nameToken.lexeme();

	cb.newField(jType, name);

	if (parser.match(TOKEN_EQUAL)) {
	    List<Token> initTokens = new ArrayList<>();

	    initTokens.add(new Token(TOKEN_EQUAL));
	    initTokens.addAll(parser.collectUntil(Arrays.asList(TOKEN_SEMICOLON, TOKEN_COMMA)));
	    initTokens.add(new Token(TOKEN_EOF));

	    FieldInitializer fi = new FieldInitializer(typeToken, nameToken, initTokens);

	    fieldInitializers.add(fi);
	}

	if (parser.match(TOKEN_COMMA)) {
	    nameToken = parser.parseVariable("Expect field name.");

	    fieldDeclaration(typeToken, nameToken);

	    return;
	}

	parser.consume(TOKEN_SEMICOLON, "Expect ';' after field declaration(s).");
    }

    private void methodDeclaration(Token typeToken, Token nameToken) {
	List<Local> params = new ArrayList<>();

	cb.newMethod(typeToken, nameToken, parameters(params));

	for (Local param : params)
	    cb.currMethod().addLocal(param, true);

	parser.consume(TOKEN_LEFT_BRACE, "Expect '{' before method body.");

	block(); // Consumes the right brace

	cb.currMethod().finish();
    }

    private void constructor() {
	cb.constructor();

	for (FieldInitializer fi : fieldInitializers) {
	    String name = fi.nameToken().lexeme();
	    Field field = cb.getField(name);

	    parser = new Parser(this, fi.initTokens());

	    parser.advance(); // to first token
	    parser.consume(TOKEN_EQUAL, "Expect '=' to begin field initialization.");

	    cb.currMethod().emitInstr(IT_LOAD_THIS);

	    expression();

	    cb.currMethod().emitInstr(IT_FIELD_STORE, field);
	}

	cb.currMethod().emitInstr(IT_RETURN);

	cb.currMethod().finish();
    }

    private String parameters(List<Local> params) {
	parser.consume(TOKEN_LEFT_PAREN, "Expect '(' after method name.");

	StringBuilder desc = new StringBuilder("(");

	if (!parser.check(TOKEN_RIGHT_PAREN)) {
	    // First pass: Parse parameters and build the method descriptor
	    do {
		Token typeToken = parser.parseType("Expect parameter type.");
		Token nameToken = parser.parseVariable("Expect parameter name.");

		String name = nameToken.lexeme();

		if (params.stream().anyMatch(local -> name.equals(local.identifier())))
		    parser.error("Already a parameter with this name for this method.");

		JType jType = JType.jTypeForLPCType(typeToken.lexeme());
		Symbol symbol = new Symbol(cb, SYM_LOCAL, jType, name, jType.descriptor());
		Local local = new Local(symbol);

		params.add(local);

		desc.append(jType.descriptor());
	    } while (parser.match(TOKEN_COMMA));
	}

	parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after method parameters.");

	return desc.append(")").toString();
    }

    public void expression() {
	parser.parsePrecedence(PREC_ASSIGNMENT, false);
    }

    private void block() {
	while (!parser.check(TOKEN_RIGHT_BRACE) && !parser.check(TOKEN_EOF))
	    declaration();

	parser.consume(TOKEN_RIGHT_BRACE, "Expect '}' after block.");
    }

    private void declaration() {
	if (parser.check(TOKEN_TYPE)) { // local
	    // parser.match(TOKEN_STAR); //temp
	    Token typeToken = parser.parseType("Expect local type.");

	    local(typeToken);
	} else // local
	    statement();

	if (parser.panicMode())
	    parser.synchronize();
    }

    private void local(Token typeToken) {
	do {
	    Token nameToken = parser.parseVariable("Expect local name.");
	    String name = nameToken.lexeme();

	    if (cb.currMethod().hasLocal(name))
		parser.error("Already a local named '" + name + "' in this scope.");

	    JType jType = JType.jTypeForLPCType(typeToken.lexeme());
	    Symbol symbol = new Symbol(cb, SYM_LOCAL, jType, name, jType.descriptor());
	    Local local = new Local(symbol);

	    int idx = cb.currMethod().addLocal(local, true);

	    if (parser.match(TOKEN_EQUAL)) {
		expression(); // leaves expression value on stack

		cb.currMethod().emitInstr(IT_LOC_STORE, idx);
	    }
	} while (parser.match(TOKEN_COMMA));

	parser.consume(TOKEN_SEMICOLON, "Expect ';' after variable declaration(s).");
    }

    private int slotForLocal(String name) {
	// traverse locals backward, looking for a match
	for (int i = cb.currMethod().locals().size() - 1; i >= 0; i--) {
	    Local local = cb.currMethod().locals().get(i);

	    if (name.equals(local.identifier())) { // found match
		if (local.scopeDepth() == -1) // "sentinel" value
		    parser.error("Can't read local variable in its own initializer.");

		return i; // runtime stack position of matching local
	    }
	} // for

	// No match; not a local.
	return -1;
    }

    private void statement() {
//	    if (parser.match(TOKEN_FOR))
//	      forStatement();
//	    else if (parser.match(TOKEN_IF))
//	      ifStatement();
//	    else if (parser.match(TOKEN_WHILE))
//	      whileStatement();
	if (parser.match(TOKEN_RETURN))
	    explicitReturnStatement();
	else if (parser.match(TOKEN_LEFT_BRACE)) {
	    beginScope();

	    block();

	    endScope();
	} else
	    expressionStatement();
    }

    // TODO: Handle implicit returns correctly.
    private void explicitReturnStatement() {
	if (parser.match(TOKEN_SEMICOLON)) { // no return value provided
	    if (cb.currMethod().jType() != JType.JVOID)
		parser.error("Missing return value.");
	    else
		cb.currMethod().emitInstr(IT_RETURN);
	} else { // handle return value
	    if (cb.currMethod().jType() == JType.JVOID)
		parser.error("Return value encountered in void method.");
	    else {
		expression();

		cb.currMethod().emitInstr(IT_RETURNVAL);

		parser.consume(TOKEN_SEMICOLON, "Expect ';' after return value.");
	    }
	} // if-else
    }

    private void expressionStatement() {
	// An expression statement is an expression in a context where a
	// statement is expected. Usually used to call a function or evaluate
	// an assignment for its side effect. The expression is evaluated
	// and the result is discarded.
	expression();

	parser.consume(TOKEN_SEMICOLON, "Expect ';' after expression.");

	// Any necessary result-popping is handled by the instruction emitters.
    }

    private void beginScope() {
	cb.currMethod().incScopeDepth();
    }

    private void endScope() {
	cb.currMethod().decScopeDepth();

	// pop all locals belonging to the expiring scope
	while (!(cb.currMethod().locals().isEmpty())
		&& cb.currMethod().locals().peek().scopeDepth() > cb.currMethod().workingScopeDepth())
	    cb.currMethod().popLocal();
    }

    //
    // Parser Callbacks
    //

    public void literal(LiteralType lType) {
	cb.currMethod().emitInstr(IT_LITERAL, lType);
    }

    public void identifier(String identifier, boolean canAssign) {
	int idx = slotForLocal(identifier);

	if (idx != -1) { // initialized local
	    if (canAssign && parser.match(TOKEN_EQUAL)) { // assignment
		cb.currMethod().emitInstr(IT_LOAD_THIS);
		expression();
		cb.currMethod().emitInstr(IT_LOC_STORE, idx);
	    } else if (parser.match(TOKEN_INVOKE)) { // method of another object
		Token nameToken = parser.parseVariable("Expect method name.");
		String methodName = nameToken.lexeme();

		cb.currMethod().emitInstr(IT_LOC_LOAD, idx);
		
		cb.currMethod().emitInstr(IT_CONST_STR, methodName);

		arguments();

		cb.currMethod().emitInstr(IT_INVOKE_OTHER);
	    } else // retrieval
		cb.currMethod().emitInstr(IT_LOC_LOAD, idx);
	} else if (cb.hasField(identifier)) { // field
	    Field field = cb.getField(identifier);

	    if (canAssign && parser.match(TOKEN_EQUAL)) { // assignment
		cb.currMethod().emitInstr(IT_LOAD_THIS);

		expression();

		cb.currMethod().emitInstr(IT_FIELD_STORE, field);
	    } else { // retrieval
		cb.currMethod().emitInstr(IT_LOAD_THIS);
		cb.currMethod().emitInstr(IT_FIELD_LOAD, field);
	    }
	} else if (cb.hasMethod(identifier)) { // method of same object
	    Method method = cb.getMethod(identifier);

	    cb.currMethod().emitInstr(IT_LOAD_THIS);

	    arguments();

	    cb.currMethod().emitInstr(IT_INVOKE, method.identifier(), method.descriptor());
	}
	// else if (resolveSuperMethod(name)) //superClass method
	// namedSuperMethod(name);
	else // method
	    parser.error("Unrecognized identifier '" + identifier + "'.");
    }

    private void arguments() {
	parser.consume(TOKEN_LEFT_PAREN, "Expect '(' after method name.");

	if (!parser.check(TOKEN_RIGHT_PAREN))
	    do
		expression();
	    while (parser.match(TOKEN_COMMA));

	parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after method arguments.");
    }

    public void lpcFloat(Float value) {
	cb.currMethod().emitInstr(IT_CONST_FLOAT, value);
    }

    public void lpcInteger(Integer value) {
	cb.currMethod().emitInstr(IT_CONST_INT, value);
    }

    public void lpcString(String value) {
	cb.currMethod().emitInstr(IT_CONST_STR, value);
    }

    public void negate() {
	cb.currMethod().emitInstr(IT_NEGATE);
    }

    public void binaryOp(BinaryOpType op) {
	cb.currMethod().emitInstr(IT_BINARY, op);
    }

    public void i2f() {
	cb.currMethod().emitInstr(IT_I2F);
    }
}