package io.github.protasm.lpc2j.scanner;

import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_BANG;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_BANG_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_COLON;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_COMMA;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_DBL_AMP;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_DBL_PIPE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_DOT;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_ELSE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_EOF;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_EQUAL_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_ERROR;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_FALSE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_FOR;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_GREATER;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_GREATER_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_IDENTIFIER;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_IF;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_INHERIT;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_INVOKE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LEFT_BRACE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LEFT_BRACKET;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LEFT_PAREN;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LESS;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_LESS_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_MINUS;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_MINUS_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_MINUS_MINUS;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_NIL;
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
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_SUPER;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_TRUE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_TYPE;
import static io.github.protasm.lpc2j.scanner.TokenType.TOKEN_WHILE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anarres.cpp.CppReader;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.StringLexerSource;

public class Scanner {
	private static final char EOL = '\n';
	private static final Map<String, TokenType> lpcTypes;
	private static final Map<String, TokenType> reservedWords;
	private static final Map<Character, TokenType> oneCharLexemes;

	private ScannableSource ss;

	static {
		lpcTypes = new HashMap<>() {
			private static final long serialVersionUID = 1L;

			{

				put("int", TOKEN_TYPE);
				put("float", TOKEN_TYPE);
				put("mapping", TOKEN_TYPE);
				put("mixed", TOKEN_TYPE);
				put("object", TOKEN_TYPE);
				put("status", TOKEN_TYPE);
				put("string", TOKEN_TYPE);
				put("void", TOKEN_TYPE);
			}
		};

		reservedWords = new HashMap<>() {
			private static final long serialVersionUID = 1L;

			{

				put("else", TOKEN_ELSE);
				put("false", TOKEN_FALSE);
				put("for", TOKEN_FOR);
				put("if", TOKEN_IF);
				put("inherit", TOKEN_INHERIT);
				put("nil", TOKEN_NIL);
				put("return", TOKEN_RETURN);
				put("true", TOKEN_TRUE);
				put("while", TOKEN_WHILE);
			}
		};

		oneCharLexemes = new HashMap<>() {
			private static final long serialVersionUID = 1L;

			{

				put('(', TOKEN_LEFT_PAREN);
				put(')', TOKEN_RIGHT_PAREN);
				put('{', TOKEN_LEFT_BRACE);
				put('}', TOKEN_RIGHT_BRACE);
				put('[', TOKEN_LEFT_BRACKET);
				put(']', TOKEN_RIGHT_BRACKET);
				put('.', TOKEN_DOT);
				put(',', TOKEN_COMMA);
				put(';', TOKEN_SEMICOLON);
			}
		};
	}

	public Scanner(String source, String sysInclPath, String quoteInclPath) {
		try (Preprocessor pp = new Preprocessor()) {
			pp.addInput(new StringLexerSource(source, true));
			pp.getSystemIncludePath().add(".");

			List<String> systemPaths = new ArrayList<>();
			systemPaths.add(sysInclPath);
			pp.setSystemIncludePath(systemPaths);

			List<String> quotePaths = new ArrayList<>();
			quotePaths.add(quoteInclPath);
			pp.setQuoteIncludePath(quotePaths);

			try (CppReader reader = new CppReader(pp)) {
				StringBuilder output = new StringBuilder();

				int ch;

				while ((ch = reader.read()) != -1) {
					output.append((char) ch);
				}

				ss = new ScannableSource(output.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<Token> scan() {
		List<Token> tokens = new ArrayList<>();

		Token token;

		do {
			token = lexToken();

			if (token != null) {
				tokens.add(token);
			}
		} while (token == null || token.type() != TOKEN_EOF);

		return tokens;
	}

	private Token lexToken() {
		if (ss.atEnd()) {
			return new Token(TOKEN_EOF, "", null, ss.line());
		}

		ss.syncTailHead();

		char c = ss.consumeOneChar();

		if (oneCharLexemes.containsKey(c)) {
			return makeToken(oneCharLexemes.get(c));
		}

		if (isDigit(c)) {
			return number();
		}

		if (isAlpha(c)) {
			return identifier();
		}

		switch (c) {
		case EOL:
		return null;
		case '"':
		return string();
		case '&':
		if (ss.match('&')) {
			return makeToken(TOKEN_DBL_AMP);
		} else {
			return unexpectedChar(c);
		}
		case '|':
		if (ss.match('|')) {
			return makeToken(TOKEN_DBL_PIPE);
		} else {
			return unexpectedChar(c);
		}
		case ':':
		if (ss.match(':')) {
			return makeToken(TOKEN_SUPER);
		} else {
			return makeToken(TOKEN_COLON);
		}
		case '-':
		if (ss.match('-')) {
			return makeToken(TOKEN_MINUS_MINUS);
		} else if (ss.match('=')) {
			return makeToken(TOKEN_MINUS_EQUAL);
		} else if (ss.match('>')) {
			return makeToken(TOKEN_INVOKE);
		} else {
			return makeToken(TOKEN_MINUS);
		}
		case '+':
		if (ss.match('+')) {
			return makeToken(TOKEN_PLUS_PLUS);
		} else if (ss.match('=')) {
			return makeToken(TOKEN_PLUS_EQUAL);
		} else {
			return makeToken(TOKEN_PLUS);
		}
		case '!':
		return makeToken(ss.match('=') ? TOKEN_BANG_EQUAL : TOKEN_BANG);
		case '=':
		return makeToken(ss.match('=') ? TOKEN_EQUAL_EQUAL : TOKEN_EQUAL);
		case '<':
		return makeToken(ss.match('=') ? TOKEN_LESS_EQUAL : TOKEN_LESS);
		case '>':
		return makeToken(ss.match('=') ? TOKEN_GREATER_EQUAL : TOKEN_GREATER);
		case '/':
		if (ss.match('/')) {
			return lineComment();
		} else if (ss.match('*')) {
			return blockComment();
		} else if (ss.match('=')) {
			return makeToken(TOKEN_SLASH_EQUAL);
		} else {
			return makeToken(TOKEN_SLASH);
		}
		case '*':
		return makeToken(ss.match('=') ? TOKEN_STAR_EQUAL : TOKEN_STAR);
		case ' ':
		case '\r':
		case '\t':
		while (isWhitespace(ss.peek())) {
			ss.advance();
		}

		return null;
		default:
		return unexpectedChar(c);
		}
	}

	private Token lineComment() {
		ss.advanceTo(EOL);

		return null;
	}

	private Token blockComment() {
		while (!ss.atEnd()) {
			ss.advanceTo('*');

			if (ss.peekPrev() == '/') {
				return errorToken("Nested block comment");
			}

			ss.advance();

			if (ss.match('/')) {
				return null;
			}
		}

		return errorToken("Unterminated block comment.");
	}

	private Token identifier() {
		while (isAlphaNumeric(ss.peek())) {
			ss.advance();
		}

		String str = ss.read();

		TokenType type = lpcTypes.get(str);

		if (type == null) {

			type = reservedWords.get(str);
		}

		if (type == null) {

			type = TOKEN_IDENTIFIER;
		}

		return makeToken(type);
	}

	private Token number() {
		boolean isFloat = false;

		while (isDigit(ss.peek())) {
			ss.advance();
		}

		if (ss.peek() == '.' && isDigit(ss.peekNext())) {
			isFloat = true;

			ss.advance();

			while (isDigit(ss.peek())) {
				ss.advance();
			}
		}

		if (isFloat) {
			return makeToken(TOKEN_NUM_FLOAT, Float.parseFloat(ss.read()));
		} else {
			return makeToken(TOKEN_NUM_INT, Integer.parseInt(ss.read()));
		}
	}

	private Token string() {
		if (!ss.advanceTo('"')) {
			return errorToken("Unterminated string.");
		}

		ss.advance();

		return makeToken(TOKEN_STRING, ss.readTrimmed());
	}

	private boolean isWhitespace(char c) {
		return (c == ' ') || (c == '\r') || (c == '\t');
	}

	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private Token unexpectedChar(char c) {
		return errorToken("Unexpected character: '" + c + "'.");
	}

	private Token errorToken(String message) {
		return new Token(TOKEN_ERROR, message, null, ss.line());
	}

	private Token makeToken(TokenType type) {
		return makeToken(type, null);
	}

	private Token makeToken(TokenType type, Object literal) {
		return new Token(type, ss.read(), literal, ss.line());
	}
}
