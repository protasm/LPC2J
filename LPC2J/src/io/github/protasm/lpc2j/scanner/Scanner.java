package io.github.protasm.lpc2j.scanner;

import static io.github.protasm.lpc2j.scanner.TokenType.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import debug.Debugger;
import org.anarres.cpp.CppReader;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.StringLexerSource;

public class Scanner implements Iterator<Token> {
  private static final char EOL = '\n';
  private static final Map<String, TokenType> lpcTypes;
  private static final Map<String, TokenType> reservedWords;
  private static final Map<Character, TokenType> oneCharLexemes;
  
  private ScannableSource ss;

  static {
    lpcTypes = new HashMap<>() {
      private static final long serialVersionUID = 1L;

      {
        // LPC Types.
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
        // Keywords.
        put("else", TOKEN_ELSE);
        put("false", TOKEN_FALSE);
        put("for", TOKEN_FOR);
        put("if", TOKEN_IF);
        put("inherit", TOKEN_INHERIT);
        put("nil", TOKEN_NIL);
        put("return", TOKEN_RETURN);
//        put("this", TOKEN_THIS);
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

  // Scanner(String, String, String)
  public Scanner(String source, String sysPath, String quotePath) {
    try (Preprocessor pp = new Preprocessor()) {
      pp.addInput(new StringLexerSource(source, true));
      pp.getSystemIncludePath().add(".");
      
      // Set Quote Include Path
      List<String> quotePaths = new ArrayList<>();
      quotePaths.add(quotePath);
      pp.setQuoteIncludePath(quotePaths);

      // Set System Include Path
      List<String> systemPaths = new ArrayList<>();
      systemPaths.add(sysPath);
      pp.setSystemIncludePath(systemPaths);

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

    //debugger.printSource(ss.toString());

    //debugger.printProgress("Scanner initialized");
  }

  // lexToken()
  public Token lexToken() {
    if (ss.atEnd())
      return new Token(TOKEN_EOF, "", null, ss.line());

    ss.syncTailHead(); // reset (tail = head)

    char c = ss.consumeOneChar();

    // one-char lexeme
    if (oneCharLexemes.containsKey(c))
      return makeToken(oneCharLexemes.get(c));

    // number
    if (isDigit(c))
      return number();

    // identifier
    if (isAlpha(c))
      return identifier();

    // symbol
    switch (c) {
    case EOL:
      return null;
    case '"':
      return string();
    case '&':
      if (ss.match('&'))
        return makeToken(TOKEN_DBL_AMP);
      else
        return unexpectedChar(c);
    case '|':
      if (ss.match('|'))
        return makeToken(TOKEN_DBL_PIPE);
      else
        return unexpectedChar(c);
    case ':':
      if (ss.match(':'))
        return makeToken(TOKEN_SUPER);
      else
        return makeToken(TOKEN_COLON);
    case '-':
      if (ss.match('-'))
        return makeToken(TOKEN_MINUS_MINUS);
      else if (ss.match('='))
        return makeToken(TOKEN_MINUS_EQUAL);
      else if (ss.match('>'))
        return makeToken(TOKEN_INVOKE);
      else
        return makeToken(TOKEN_MINUS);
    case '+':
      if (ss.match('+'))
        return makeToken(TOKEN_PLUS_PLUS);
      else if (ss.match('='))
        return makeToken(TOKEN_PLUS_EQUAL);
      else
        return makeToken(TOKEN_PLUS);
    case '!':
      return makeToken(ss.match('=') ? TOKEN_BANG_EQUAL : TOKEN_BANG);
    case '=':
      return makeToken(ss.match('=') ? TOKEN_EQUAL_EQUAL : TOKEN_EQUAL);
    case '<':
      return makeToken(ss.match('=') ? TOKEN_LESS_EQUAL : TOKEN_LESS);
    case '>':
      return makeToken(ss.match('=') ? TOKEN_GREATER_EQUAL : TOKEN_GREATER);
    case '/':
      if (ss.match('/'))
        return lineComment();
      else if (ss.match('*'))
        return blockComment();
      else if (ss.match('='))
        return makeToken(TOKEN_SLASH_EQUAL);
      else
        return makeToken(TOKEN_SLASH);
    case '*':
      return makeToken(ss.match('=') ? TOKEN_STAR_EQUAL : TOKEN_STAR);
    case ' ':
    case '\r':
    case '\t':
      while (isWhitespace(ss.peek()))
        ss.advance(); // fast-forward through whitespace

      return null;
    default:
      return unexpectedChar(c);
    } // switch
  }

  // lineComment()
  private Token lineComment() {
    ss.advanceTo(EOL);

    return null;
  }

  // blockComment()
  private Token blockComment() {
    while (!ss.atEnd()) {
      ss.advanceTo('*');

      if (ss.peekPrev() == '/')
        return errorToken("Nested block comment");

      ss.advance();

      if (ss.match('/'))
        return null;
    } // while

    // Error if we get here.
    return errorToken("Unterminated block comment.");
  }

  // identifier()
  private Token identifier() {
    while (isAlphaNumeric(ss.peek()))
      ss.advance();

    String str = ss.read();

    // check LPC types first
    TokenType type = lpcTypes.get(str);

    if (type == null)
      // check reserved words next
      type = reservedWords.get(str);

    if (type == null)
      // treat as identifier
      type = TOKEN_IDENTIFIER;

    return makeToken(type);
  }

  // number()
  private Token number() {
	boolean isFloat = false;

    while (isDigit(ss.peek()))
      ss.advance();

    // Look for a fractional part.
    if (ss.peek() == '.' && isDigit(ss.peekNext())) {
      isFloat = true;

      ss.advance(); // consume the '.'

      while (isDigit(ss.peek()))
        ss.advance();
    }

    if (isFloat)
      return makeToken(TOKEN_NUM_FLOAT, Float.parseFloat(ss.read()));
    else
      return makeToken(TOKEN_NUM_INT, Integer.parseInt(ss.read()));
  }

  // string()
  private Token string() {
    if (!ss.advanceTo('"'))
      return errorToken("Unterminated string.");

    ss.advance(); // consume the closing '"'

    return makeToken(TOKEN_STRING, ss.readTrimmed());
  }

  // isWhitespace(char)
  private boolean isWhitespace(char c) {
    return (c == ' ') || (c == '\r') || (c == '\t');
  }

  // isAlpha(char)
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  // isAlphaNumeric(char)
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  // isDigit(char)
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  // unexpectedChar(char)
  private Token unexpectedChar(char c) {
    return errorToken("Unexpected character: '" + c + "'.");
  }

  // errorToken(String)
  private Token errorToken(String message) {
    return new Token(TOKEN_ERROR, message, null, ss.line());
  }

  // makeToken(TokenType)
  private Token makeToken(TokenType type) {
    return makeToken(type, null);
  }

  // makeToken(TokenType, Object)
  private Token makeToken(TokenType type, Object literal) {
    return new Token(type, ss.read(), literal, ss.line());
  }

  // hasNext()
  @Override
  public boolean hasNext() {
    return true;
  }

  // next()
  @Override
  public Token next() {
    Token token;

    do {
      token = lexToken();
    } while (token == null);

    return token;
  }

  // remove()
  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
