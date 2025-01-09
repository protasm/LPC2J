package io.github.protasm.lpc2j;

import static io.github.protasm.lpc2j.InstrType.BINARY;
import static io.github.protasm.lpc2j.InstrType.CALL;
import static io.github.protasm.lpc2j.InstrType.CONST_FLOAT;
import static io.github.protasm.lpc2j.InstrType.CONST_INT;
import static io.github.protasm.lpc2j.InstrType.CONST_STR;
import static io.github.protasm.lpc2j.InstrType.FIELD_LOAD;
import static io.github.protasm.lpc2j.InstrType.FIELD_STORE;
import static io.github.protasm.lpc2j.InstrType.I2F;
import static io.github.protasm.lpc2j.InstrType.LOC_LOAD;
import static io.github.protasm.lpc2j.InstrType.LOC_STORE;
import static io.github.protasm.lpc2j.InstrType.NEGATE;
import static io.github.protasm.lpc2j.InstrType.RETURN;
import static io.github.protasm.lpc2j.InstrType.RETURNVAL;
import static io.github.protasm.lpc2j.InstrType.THIS;
import static io.github.protasm.lpc2j.parser.Parser.Precedence.PREC_ASSIGNMENT;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_COMMA;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_EOF;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LEFT_BRACE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LEFT_PAREN;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RETURN;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RIGHT_BRACE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_RIGHT_PAREN;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_SEMICOLON;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_TYPE;

import java.io.FileOutputStream;
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
	cb = new ClassBuilder(sourceFile.className());

	Scanner scanner = new Scanner(sourceFile.source(), sysIncludePath, quoteIncludePath);
	parser = new Parser(this, scanner.scan());
	fieldInitializers = new ArrayList<>();

	parser.advance(); // to first token

	while (!parser.match(TOKEN_EOF)) {
	    member();
	}

	constructor();

	cb.finish();

	return cb.bytes();
    }

    private void member() {
	Token typeToken = parser.parseType("Expect member type.");
	Token nameToken = parser.parseVariable("Expect member name.");

	if (parser.check(TOKEN_LEFT_PAREN)) {
	    method(typeToken, nameToken);
	} else {
	    field(typeToken, nameToken);
	}

	if (parser.panicMode()) {
	    parser.synchronize();
	}
    }

    private void field(Token typeToken, Token nameToken) {
	cb.field(new Field(typeToken, nameToken));

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

	    field(typeToken, nameToken);

	    return;
	}

	parser.consume(TOKEN_SEMICOLON, "Expect ';' after field declaration(s).");
    }

    private void method(Token typeToken, Token nameToken) {
	List<Local> params = new ArrayList<>();

	parser.consume(TOKEN_LEFT_PAREN, "Expect '(' after method name.");

	cb.newMethod(typeToken, nameToken, parameters(params));

	for (Local param : params) {
	    cb.mb().addLocal(param, true);
	}

	parser.consume(TOKEN_LEFT_BRACE, "Expect '{' before method body.");

	block(); // Consumes the right brace

	cb.mb().finish();
    }

    private void constructor() {
	cb.constructor();

	for (FieldInitializer fi : fieldInitializers) {
	    String name = fi.nameToken().lexeme();
	    Field field = cb.getField(name);

	    parser = new Parser(this, fi.initTokens());

	    parser.advance(); // to first token
	    parser.consume(TOKEN_EQUAL, "Expect '=' to begin field initialization.");

	    cb.mb().emitInstr(THIS);

	    expression();

	    cb.mb().emitInstr(FIELD_STORE, field);
	}

	cb.mb().emitInstr(RETURN);

	cb.mb().finish();
    }

    private String parameters(List<Local> params) {
	StringBuilder desc = new StringBuilder("(");

	if (!parser.check(TOKEN_RIGHT_PAREN)) {
	    // First pass: Parse parameters and build the method descriptor
	    do {
		Token typeToken = parser.parseType("Expect parameter type.");
		Token nameToken = parser.parseVariable("Expect parameter name.");

		String name = nameToken.lexeme();

		if (params.stream().anyMatch(local -> name.equals(local.name()))) {
		    parser.error("Already a parameter with this name for this method.");
		}

		String lpcType = typeToken.lexeme();
		JType jType = JType.jTypeForLPCType(lpcType);
		Local local = new Local(jType, name);

		params.add(local);

		desc.append(JType.jDescForLPCType(lpcType));
	    } while (parser.match(TOKEN_COMMA));
	}

	parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after method parameters.");

	return desc.append(")").toString();
    }

    public void expression() {
	parser.parsePrecedence(PREC_ASSIGNMENT, false);
    }

    private void block() {
	while (!parser.check(TOKEN_RIGHT_BRACE) && !parser.check(TOKEN_EOF)) {
	    declaration();
	}

	parser.consume(TOKEN_RIGHT_BRACE, "Expect '}' after block.");
    }

    private void declaration() {
	if (parser.check(TOKEN_TYPE)) { // local
	    // parser.match(TOKEN_STAR); //temp
	    Token typeToken = parser.parseType("Expect local type.");

	    local(typeToken);
	} else { // local
		statement();
	}

	if (parser.panicMode()) {
	    parser.synchronize();
	}
    }

    private void local(Token typeToken) {
	do {
	    Token nameToken = parser.parseVariable("Expect local name.");
	    String name = nameToken.lexeme();

	    if (cb.mb().hasLocal(name)) {
		parser.error("Already a local named '" + name + "' in this scope.");
	    }

	    String lpcType = typeToken.lexeme();
	    JType jType = JType.jTypeForLPCType(lpcType);
	    Local local = new Local(jType, name);

	    int idx = cb.mb().addLocal(local, true);

	    if (parser.match(TOKEN_EQUAL)) {
		expression(); // leaves expression value on stack

		cb.mb().emitInstr(LOC_STORE, idx);
	    }
	} while (parser.match(TOKEN_COMMA));

	parser.consume(TOKEN_SEMICOLON, "Expect ';' after variable declaration(s).");
    }

    private int slotForLocal(String name) {
	// traverse locals backward, looking for a match
	for (int i = cb.mb().locals().size() - 1; i >= 0; i--) {
	    Local local = cb.mb().locals().get(i);

	    if (name.equals(local.name())) { // found match
		if (local.scopeDepth() == -1) { // "sentinel" value
			parser.error("Can't read local variable in its own initializer.");
		}

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
	if (parser.match(TOKEN_RETURN)) {
	    explicitReturnStatement();
	} else if (parser.match(TOKEN_LEFT_BRACE)) {
	    beginScope();

	    block();

	    endScope();
	} else {
	    expressionStatement();
	}
    }

    // TODO: Handle implicit returns correctly.
    private void explicitReturnStatement() {
	if (parser.match(TOKEN_SEMICOLON)) { // no return value provided
	    if (cb.mb().jType() != JType.JVOID) {
		parser.error("Missing return value.");
	    } else {
		cb.mb().emitInstr(RETURN);
	    }
	} else { // handle return value
	    if (cb.mb().jType() == JType.JVOID) {
		parser.error("Return value encountered in void method.");
	    } else {
		expression();

		cb.mb().emitInstr(RETURNVAL);

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
	cb.mb().incScopeDepth();
    }

    private void endScope() {
	cb.mb().decScopeDepth();

	// pop all locals belonging to the expiring scope
	while (!(cb.mb().locals().isEmpty()) && cb.mb().locals().peek().scopeDepth() > cb.mb().workingScopeDepth()) {
	    cb.mb().popLocal();
	}
    }

    //
    // Parser Callbacks
    //

    public void variable(String name, boolean canAssign) {
	int idx = slotForLocal(name);

	if (idx != -1) { // initialized local
		if (canAssign && parser.match(TOKEN_EQUAL)) { // assignment
		cb.mb().emitInstr(THIS);
		expression();
		cb.mb().emitInstr(LOC_STORE, idx);
		} else // retrieval
		cb.mb().emitInstr(LOC_LOAD, idx);
	} else if (cb.hasField(name)) { // field
	    Field field = cb.getField(name);

	    if (canAssign && parser.match(TOKEN_EQUAL)) { // assignment
		cb.mb().emitInstr(THIS);

		expression();

		cb.mb().emitInstr(FIELD_STORE, field);
	    } else { // retrieval
		cb.mb().emitInstr(THIS);
		cb.mb().emitInstr(FIELD_LOAD, field);
	    }
	} else if (cb.hasMethod(name)) { // method
	    Method method = cb.getMethod(name);

	    cb.mb().emitInstr(THIS);

	    parser.consume(TOKEN_LEFT_PAREN, "Expect '(' after method name.");

	    if (!parser.check(TOKEN_RIGHT_PAREN)) {
		do {
		    expression();
		} while (parser.match(TOKEN_COMMA));
	    }

	    parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after method arguments.");

	    cb.mb().emitInstr(CALL, cb.name(), method.name(), method.desc());
	}
	// else if (resolveSuperMethod(name)) //superClass method
	// namedSuperMethod(name);
 else { // method
		parser.error("Unrecognized variable '" + name + "'.");
	}
    }

    public void lpcFloat(Float value) {
	cb.mb().emitInstr(CONST_FLOAT, value);
    }

    public void lpcInteger(Integer value) {
	cb.mb().emitInstr(CONST_INT, value);
    }

    public void lpcString(String value) {
	cb.mb().emitInstr(CONST_STR, value);
    }

    public void negate() {
	cb.mb().emitInstr(NEGATE);
    }

    public void binaryOp(BinaryOpType op) {
	cb.mb().emitInstr(BINARY, op);
    }

    public void i2f() {
	cb.mb().emitInstr(I2F);
    }

    public static void main(String[] args) throws IOException {
	if (args.length != 1) {
	    System.out.println("Usage: LPC2J <path>");

	    return;
	}

	LPC2J compiler = new LPC2J();
	SourceFile sourceFile = new SourceFile(args[0]);

	byte[] bytes = compiler.compile(sourceFile);

	try (FileOutputStream fos = new FileOutputStream(sourceFile.outputPath())) {
	    fos.write(bytes);
	    System.out.println("Byte array written to: " + sourceFile.outputPath());
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}