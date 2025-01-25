package io.github.protasm.lpc2j.scanner;

import static io.github.protasm.lpc2j.scanner.TokenType.T_BANG;
import static io.github.protasm.lpc2j.scanner.TokenType.T_BANG_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_COLON;
import static io.github.protasm.lpc2j.scanner.TokenType.T_COMMA;
import static io.github.protasm.lpc2j.scanner.TokenType.T_DBL_AMP;
import static io.github.protasm.lpc2j.scanner.TokenType.T_DBL_PIPE;
import static io.github.protasm.lpc2j.scanner.TokenType.T_ELSE;
import static io.github.protasm.lpc2j.scanner.TokenType.T_EOF;
import static io.github.protasm.lpc2j.scanner.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_EQUAL_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_ERROR;
import static io.github.protasm.lpc2j.scanner.TokenType.T_FALSE;
import static io.github.protasm.lpc2j.scanner.TokenType.T_FOR;
import static io.github.protasm.lpc2j.scanner.TokenType.T_GREATER;
import static io.github.protasm.lpc2j.scanner.TokenType.T_GREATER_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.scanner.TokenType.T_IF;
import static io.github.protasm.lpc2j.scanner.TokenType.T_INHERIT;
import static io.github.protasm.lpc2j.scanner.TokenType.T_INVOKE;
import static io.github.protasm.lpc2j.scanner.TokenType.T_LEFT_BRACE;
import static io.github.protasm.lpc2j.scanner.TokenType.T_LEFT_BRACKET;
import static io.github.protasm.lpc2j.scanner.TokenType.T_LEFT_PAREN;
import static io.github.protasm.lpc2j.scanner.TokenType.T_LESS;
import static io.github.protasm.lpc2j.scanner.TokenType.T_LESS_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_MINUS;
import static io.github.protasm.lpc2j.scanner.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_MINUS_MINUS;
import static io.github.protasm.lpc2j.scanner.TokenType.T_NIL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_FLOAT_LITERAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_INT_LITERAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_PLUS;
import static io.github.protasm.lpc2j.scanner.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_PLUS_PLUS;
import static io.github.protasm.lpc2j.scanner.TokenType.T_RETURN;
import static io.github.protasm.lpc2j.scanner.TokenType.T_RIGHT_BRACE;
import static io.github.protasm.lpc2j.scanner.TokenType.T_RIGHT_BRACKET;
import static io.github.protasm.lpc2j.scanner.TokenType.T_RIGHT_PAREN;
import static io.github.protasm.lpc2j.scanner.TokenType.T_SEMICOLON;
import static io.github.protasm.lpc2j.scanner.TokenType.T_SLASH;
import static io.github.protasm.lpc2j.scanner.TokenType.T_SLASH_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_STAR;
import static io.github.protasm.lpc2j.scanner.TokenType.T_STAR_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_STRING_LITERAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_SUPER;
import static io.github.protasm.lpc2j.scanner.TokenType.T_TRUE;
import static io.github.protasm.lpc2j.scanner.TokenType.T_TYPE;
import static io.github.protasm.lpc2j.scanner.TokenType.T_WHILE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anarres.cpp.CppReader;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.StringLexerSource;

import io.github.protasm.lpc2j.LPCType;

public class Scanner {
    private static final char EOL = '\n';
    private static final Map<String, LPCType> lpcTypeWords;
    private static final Map<String, TokenType> reservedWords;
    private static final Map<Character, TokenType> oneCharLexemes;
    private ScannableSource ss;

    static {
	lpcTypeWords = new HashMap<>() {
	    private static final long serialVersionUID = 1L;
	    {
		put("int", LPCType.LPCINT);
		put("float", LPCType.LPCFLOAT);
		put("mapping", LPCType.LPCMAPPING);
		put("mixed", LPCType.LPCMIXED);
		put("object", LPCType.LPCOBJECT);
		put("status", LPCType.LPCSTATUS);
		put("string", LPCType.LPCSTRING);
		put("void", LPCType.LPCVOID);
	    }
	};

	reservedWords = new HashMap<>() {
	    private static final long serialVersionUID = 1L;

	    {
		put("else", T_ELSE);
		put("false", T_FALSE);
		put("for", T_FOR);
		put("if", T_IF);
		put("inherit", T_INHERIT);
		put("nil", T_NIL);
		put("return", T_RETURN);
		put("true", T_TRUE);
		put("while", T_WHILE);
	    }
	};

	oneCharLexemes = new HashMap<>() {
	    private static final long serialVersionUID = 1L;

	    {
		put('(', T_LEFT_PAREN);
		put(')', T_RIGHT_PAREN);
		put('{', T_LEFT_BRACE);
		put('}', T_RIGHT_BRACE);
		put('[', T_LEFT_BRACKET);
		put(']', T_RIGHT_BRACKET);
		put(',', T_COMMA);
		put(';', T_SEMICOLON);
	    }
	};
    }
    
    public Scanner(String source) {
	this(source, ".", ".");
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

		while ((ch = reader.read()) != -1)
		    output.append((char) ch);

		ss = new ScannableSource(output.toString());
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public TokenList scan() {
	TokenList tokens = new TokenList();
	Token<?> token;

	do {
	    token = lexToken();

	    if (token != null)
		tokens.add(token);
	} while (token == null || token.type() != T_EOF);

	return tokens;
    }

    private Token<?> lexToken() {
	if (ss.atEnd())
	    return token(T_EOF);

	ss.syncTailHead();

	char c = ss.consumeOneChar();

	if (oneCharLexemes.containsKey(c))
	    return token(oneCharLexemes.get(c));

	if (isDigit(c))
	    return number();

	if (isAlpha(c))
	    return identifier();

	switch (c) {
	case EOL:
	    return null;
	case '"':
	    return stringLiteral();
	case '&':
	    if (ss.match('&'))
		return token(T_DBL_AMP);
	    else
		return unexpectedChar(c);
	case '|':
	    if (ss.match('|'))
		return token(T_DBL_PIPE);
	    else
		return unexpectedChar(c);
	case ':':
	    if (ss.match(':'))
		return token(T_SUPER);
	    else
		return token(T_COLON);
	case '-':
	    if (ss.match('-'))
		return token(T_MINUS_MINUS);
	    else if (ss.match('='))
		return token(T_MINUS_EQUAL);
	    else if (ss.match('>'))
		return token(T_INVOKE);
	    else
		return token(T_MINUS);
	case '+':
	    if (ss.match('+'))
		return token(T_PLUS_PLUS);
	    else if (ss.match('='))
		return token(T_PLUS_EQUAL);
	    else
		return token(T_PLUS);
	case '!':
	    return token(ss.match('=') ? T_BANG_EQUAL : T_BANG);
	case '=':
	    return token(ss.match('=') ? T_EQUAL_EQUAL : T_EQUAL);
	case '<':
	    return token(ss.match('=') ? T_LESS_EQUAL : T_LESS);
	case '>':
	    return token(ss.match('=') ? T_GREATER_EQUAL : T_GREATER);
	case '/':
	    if (ss.match('/'))
		return lineComment();
	    else if (ss.match('*'))
		return blockComment();
	    else if (ss.match('='))
		return token(T_SLASH_EQUAL);
	    else
		return token(T_SLASH);
	case '*':
	    return token(ss.match('=') ? T_STAR_EQUAL : T_STAR);
	case ' ':
	case '\r':
	case '\t':
	    while (isWhitespace(ss.peek()))
		ss.advance();

	    return null;
	default:
	    return unexpectedChar(c);
	}
    }

    private Token<?> lineComment() {
	ss.advanceTo(EOL);

	return null;
    }

    private Token<?> blockComment() {
	while (!ss.atEnd()) {
	    ss.advanceTo('*');

	    if (ss.peekPrev() == '/')
		return errorToken("Nested block comment");

	    ss.advance();

	    if (ss.match('/'))
		return null;
	}

	return errorToken("Unterminated block comment.");
    }

    private Token<?> identifier() {
	while (isAlphaNumeric(ss.peek()))
	    ss.advance();

	String str = ss.read();

	if (lpcTypeWords.keySet().contains(str))
	    return typeToken(str);

	TokenType type = reservedWords.get(str);

	if (type == null)
	    type = T_IDENTIFIER;

	return token(type);
    }

    private Token<?> number() {
	boolean isFloat = false;

	while (isDigit(ss.peek()))
	    ss.advance();

	if (ss.peek() == '.' && isDigit(ss.peekNext())) {
	    isFloat = true;

	    ss.advance();

	    while (isDigit(ss.peek()))
		ss.advance();
	}

	String lexeme = ss.read();

	if (isFloat)
	    return floatToken(T_FLOAT_LITERAL, lexeme, Float.parseFloat(lexeme));
	else
	    return intToken(T_INT_LITERAL, lexeme, Integer.parseInt(lexeme));
    }

    private Token<String> stringLiteral() {
	if (!ss.advanceTo('"'))
	    return errorToken("Unterminated string.");

	ss.advance();

	return stringToken(T_STRING_LITERAL, ss.readTrimmed());
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

    private Token<String> unexpectedChar(char c) {
	return errorToken("Unexpected character: '" + c + "'.");
    }

    private Token<Object> token(TokenType type) {
	return new Token<>(type, ss.read(), null, ss.line());
    }

    private Token<String> errorToken(String message) {
	return new Token<>(T_ERROR, message, null, ss.line());
    }

    private Token<Integer> intToken(TokenType type, String lexeme, Integer i) {
	return new Token<>(type, lexeme, i, ss.line());
    }

    private Token<Float> floatToken(TokenType type, String lexeme, Float f) {
	return new Token<>(type, lexeme, f, ss.line());
    }

    private Token<String> stringToken(TokenType type, String literal) {
	return new Token<>(type, ss.read(), literal, ss.line());
    }

    private Token<LPCType> typeToken(String lexeme) {
	LPCType type = lpcTypeWords.get(lexeme);

	return new Token<>(T_TYPE, lexeme, type, ss.line());
    }

    public static void main(String[] args) {
	if (args.length != 1) {
	    System.err.println("Usage: java Scanner <file>");

	    System.exit(1);
	}

	String fileName = args[0];
	Path filePath = Paths.get(fileName);
	String source;

	try {
	    source = Files.readString(filePath);
	} catch (IOException e) {
	    System.err.println("Error: Unable to locate or read file '" + fileName + "'");
	    System.exit(1);
	    return; // Unreachable, but included for clarity
	}

	// Create a Scanner and scan the tokens
	Scanner scanner = new Scanner(source);
	TokenList tokens = scanner.scan();

	// Print tokens grouped by lines
	int currentLine = -1;
	StringBuilder lineBuffer = new StringBuilder();

	for (int i = 0; i < tokens.size(); i++) {
	    Token<?> token = tokens.get(i);

	    // Check if we're starting a new line
	    if (token.line() != currentLine) {
		// Print the buffered line if moving to a new line
		if (lineBuffer.length() > 0) {
		    System.out.println(lineBuffer.toString());
		    lineBuffer.setLength(0); // Clear the buffer
		}

		// Update current line
		currentLine = token.line();
	    }

	    // Append the token to the current line
	    lineBuffer.append(token).append(" ");
	}

	// Print the final line if there's anything left in the buffer
	if (lineBuffer.length() > 0)
	    System.out.println(lineBuffer.toString());
    }
}
