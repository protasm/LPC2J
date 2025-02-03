package io.github.protasm.lpc2j;

import static io.github.protasm.lpc2j.InstrType.*;
import static io.github.protasm.lpc2j.JType.JLPCOBJECT;
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

import io.github.protasm.lpc2j.fs.FSFile;
import io.github.protasm.lpc2j.parser.Local;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.scanner.Token;

public class LPC2J_old {
	private String sysIncludePath;
	private String quoteIncludePath;

	private ClassBuilder cb;

	private Parser parser;
	private List<FieldInitializer> fieldInitializers;

	public LPC2J_old(String sysIncludePath, String quoteIncludePath) {
		this.sysIncludePath = sysIncludePath;
		this.quoteIncludePath = quoteIncludePath;
	}

	public LPC2J_old() {
		this(".", ".");
	}

	public ClassBuilder cb() {
		return cb;
	}

	public Parser parser() {
		return parser;
	}

	public byte[] compile(FSFile sourceFile) throws IOException {
		String fullClassName = "io/github/protasm/brainjar/lpc/" + sourceFile.dotName();

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

	//
	// Parser Callbacks
	//

	public void literal(LiteralType lType) {
		cb.currMethod().emitInstr(IT_LITERAL, lType);
	}

	public void identifier(String identifier, boolean canAssign) {
		int idx = get(identifier);

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

				arguments(true);

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

			arguments(false);

			cb.currMethod().emitInstr(IT_INVOKE, method.identifier(), method.descriptor());
		}
		// else if (resolveSuperMethod(name)) //superClass method
		// namedSuperMethod(name);
		else // method
			parser.error("Unrecognized identifier '" + identifier + "'.");
	}

	private void arguments(boolean asArray) {
		parser.consume(TOKEN_LEFT_PAREN, "Expect '(' after method name.");

		if (asArray)
			cb.currMethod().emitInstr(IT_NEW_ARRAY, "java/lang/Object");

		int currIdx = 0; // Track the argument index

		if (!parser.check(TOKEN_RIGHT_PAREN)) {
			do {
				// Emit bytecode for the current argument expression
				expression();

				if (asArray) {
					// Store the current argument in the array
					methodVisitor.visitInsn(Opcodes.DUP); // Duplicate the array reference
					methodVisitor.visitLdcInsn(currIdx); // Push the current index
					methodVisitor.visitInsn(Opcodes.AASTORE); // Store the argument in the array
				}

				currIdx++;
			} while (parser.match(TOKEN_COMMA));
		}

		parser.consume(TOKEN_RIGHT_PAREN, "Expect ')' after method arguments.");
	}
}