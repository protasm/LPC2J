package io.github.protasm.lpc2j.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.parselet.*;
import io.github.protasm.lpc2j.scanner.Scanner;
import io.github.protasm.lpc2j.scanner.Token;
import io.github.protasm.lpc2j.scanner.TokenType;

import static io.github.protasm.lpc2j.parser.Parser.Precedence.*;
import static io.github.protasm.lpc2j.scanner.TokenType.*;

public class Parser {
  public static final class Precedence {
    public static final int PREC_NONE       = 0;
    public static final int PREC_ASSIGNMENT = 1;  // =, -=, +=
    public static final int PREC_OR         = 2;  // or
    public static final int PREC_AND        = 3;  // and
    public static final int PREC_EQUALITY   = 4;  // ==, !=
    public static final int PREC_COMPARISON = 5;  // <, >, <=, >=
    public static final int PREC_TERM       = 6;  // +, -
    public static final int PREC_FACTOR     = 7;  // *, /
    public static final int PREC_UNARY      = 8;  // !, -
    public static final int PREC_INDEX      = 9;  // []
    public static final int PREC_CALL       = 10; // ->, ()
    public static final int PREC_PRIMARY    = 11;

    //Precedence()
    private Precedence() {}
  }

  private LPC2J compiler;
  private Iterator<Token> tokens;

  private Map<TokenType, ParseRule> tokenTypeToRule;

  private Token previous;
  private Token current;
  private boolean hadError;
  private boolean panicMode;

  //Parser(LPC2J, String)
  public Parser(LPC2J compiler, String source, String sysPath, String quotePath) {
    this.compiler = compiler;
    tokens = new Scanner(source, sysPath, quotePath);

    tokenTypeToRule = new HashMap<>();

    registerTokenTypesToRules();

    previous = null;
    current = null;
    hadError = false;
    panicMode = false;
  } //Parser(LPC2J, String)

  //previous()
  public Token previous() {
    return previous;
  }

  //current()
  public Token current() {
    return current;
  }

  //setCurrent()
  public void setCurrent(Token token) {
    current = token;
  }

  //transformCurrent(TokenType)
  public void transformCurrent(TokenType type) {
    current = new Token(type, null, null, current.line());
  }

  //hadError()
  public boolean hadError() {
    return hadError;
  }

  //panicMode()
  public boolean panicMode() {
    return panicMode;
  }

  //synchronize()
  public void synchronize() {
    panicMode = false;

    while (current.type() != TOKEN_EOF) {
      if (previous.type() == TOKEN_SEMICOLON) return;

      switch (current.type()) {
        case TOKEN_TYPE:
        case TOKEN_FOR:
        case TOKEN_IF:
        case TOKEN_WHILE:
        case TOKEN_RETURN:
          return;

        default:
          break; //Do nothing.
      } //switch

      advance();
    } //while
  }

  //advance()
  public void advance() {
    previous = current;

    for (;;) {
      current = tokens.next();

      if (current.type() != TOKEN_ERROR)
        break;

      errorAtCurrent(current.lexeme());
    }
  }

  //match(TokenType)
  public boolean match(TokenType type) {
    if (!check(type)) return false;

    advance();

    return true;
  }

  //check(TokenType)
  public boolean check(TokenType type) {
    return current.type() == type;
  }

  //consume(TokenType, String)
  public void consume(TokenType type, String message) {
    if (current.type() == type) {
      advance();

      return;
    }

    errorAtCurrent(message);
  }

  //errorAtCurrent(String)
  public void errorAtCurrent(String message) {
    errorAt(current, message);
  }

  //error(String)
  public void error(String message) {
    errorAt(previous, message);
  }

  //errorAt(Token, String)
  private void errorAt(Token token, String message) {
    if (panicMode) return;

    panicMode = true;

    System.err.print("[line " + token.line() + "] Error");

    if (token.type() == TOKEN_EOF)
      System.err.print(" at end");
    else if (token.type() == TOKEN_ERROR) {
      // Nothing.
    } else
      System.err.print(" at '" + token.lexeme() + "'");

    System.err.print(": " + message + "\n");

    hadError = true;
  }

  //parsePrecedence(int)
  public void parsePrecedence(int precedence) {
    advance();

    // Look up the Parselet to use where the previous token's type
    // is an expression prefix.
    Parselet prefixParselet = getRule(previous.type()).prefix();

    if (prefixParselet == null) {
      error("Expect expression.");

      return;
    }

    boolean canAssign = (precedence <= PREC_ASSIGNMENT);

    prefixParselet.parse(this, compiler, canAssign);

    //infix parsing loop
    while (precedence <= getRule(current.type()).precedence()) {
      advance();

      Parselet infixParselet = getRule(previous.type()).infix();

      infixParselet.parse(this, compiler, canAssign);
    }

    if (canAssign)
      if (match(TOKEN_EQUAL) || match(TOKEN_PLUS_EQUAL))
        //If the = doesn't get consumed as part of the expression, nothing
        //else is going to consume it. It's an error and we should report it.
        error("Invalid assignment target.");
  }

  //register(TokenType, Parselet, Parselet, int)
  private void register(TokenType type, Parselet prefix, Parselet infix, int precedence) {
    tokenTypeToRule.put(type, new ParseRule(prefix, infix, precedence));
  }

  //getRule(TokenType)
  public ParseRule getRule(TokenType type) {
    return tokenTypeToRule.get(type);
  }

  //registerTokenTypesToRules()
  private void registerTokenTypesToRules() {
    //Column 1: TokenType
    //Column 2: Parselet to use when TokenType appears as expression prefix
    //Column 3: Parselet to use when TokenType appears as expression infix
    //Column 4: Precedence to use when TokenType appears as expression infix
	  
	//**PREFIX**
    //register(TOKEN_BANG,       new UnaryParselet(),    null, PREC_NONE);
    register(TOKEN_FALSE,      new LiteralParselet(),  null, PREC_NONE);
    register(TOKEN_IDENTIFIER, new VariableParselet(), null, PREC_NONE);
    //register(TOKEN_NIL,        new LiteralParselet(),  null, PREC_NONE);
    register(TOKEN_NUM_INT,    new NumberParselet(),   null, PREC_NONE);
    register(TOKEN_NUM_FLOAT,  new NumberParselet(),   null, PREC_NONE);
    register(TOKEN_STRING,     new StringParselet(),   null, PREC_NONE);
    //register(TOKEN_SUPER,      new SuperParselet(),    null, PREC_NONE);
    //register(TOKEN_THIS,       new ThisParselet(),     null, PREC_NONE);
    register(TOKEN_TRUE,       new LiteralParselet(),  null, PREC_NONE);

    //**INFIX**
    //register(TOKEN_BANG_EQUAL,    null, new BinaryParselet(), PREC_EQUALITY);
    //register(TOKEN_DBL_AMP,       null, new AndParselet(),    PREC_AND);
    //register(TOKEN_DBL_PIPE,      null, new OrParselet(),     PREC_OR);
    //register(TOKEN_DOT,           null, new DotParselet(),    PREC_CALL);
    //register(TOKEN_EQUAL_EQUAL,   null, new BinaryParselet(), PREC_EQUALITY);
    //register(TOKEN_GREATER,       null, new BinaryParselet(), PREC_COMPARISON);
    //register(TOKEN_GREATER_EQUAL, null, new BinaryParselet(), PREC_COMPARISON);
    //register(TOKEN_INVOKE,        null, new InvokeParselet(), PREC_CALL);
    //register(TOKEN_LEFT_BRACKET,  null, new IndexParselet(),  PREC_INDEX);
    //register(TOKEN_LESS,          null, new BinaryParselet(), PREC_COMPARISON);
//    register(TOKEN_LESS_EQUAL,    null, new BinaryParselet(), PREC_COMPARISON);
    register(TOKEN_PLUS,          null, new BinaryParselet(), PREC_TERM);
    //register(TOKEN_SLASH,         null, new BinaryParselet(), PREC_FACTOR);
    //register(TOKEN_STAR,          null, new BinaryParselet(), PREC_FACTOR);

    //**BOTH**
    //register(TOKEN_LEFT_PAREN, new LParenParselet(), new CallParselet(),   PREC_CALL);
    register(TOKEN_MINUS,      new UnaryParselet(),  new BinaryParselet(), PREC_TERM);

    //**NEITHER**
    register(TOKEN_COLON,         null, null, PREC_NONE);
    register(TOKEN_COMMA,         null, null, PREC_NONE);
    register(TOKEN_DOT,           null, null, PREC_NONE);
    register(TOKEN_ELSE,          null, null, PREC_NONE);
    register(TOKEN_EOF,           null, null, PREC_NONE);
    register(TOKEN_EQUAL,         null, null, PREC_NONE);
    register(TOKEN_ERROR,         null, null, PREC_NONE);
    register(TOKEN_FOR,           null, null, PREC_NONE);
    register(TOKEN_IF,            null, null, PREC_NONE);
    register(TOKEN_INHERIT,       null, null, PREC_NONE);
    register(TOKEN_LEFT_BRACE,    null, null, PREC_NONE);
    register(TOKEN_MINUS_EQUAL,   null, null, PREC_NONE);
    register(TOKEN_MINUS_MINUS,   null, null, PREC_NONE);
    register(TOKEN_PLUS_EQUAL,    null, null, PREC_NONE);
    register(TOKEN_PLUS_PLUS,     null, null, PREC_NONE);
    register(TOKEN_RETURN,        null, null, PREC_NONE);
    register(TOKEN_RIGHT_BRACE,   null, null, PREC_NONE);
    register(TOKEN_RIGHT_BRACKET, null, null, PREC_NONE);
    register(TOKEN_RIGHT_PAREN,   null, null, PREC_NONE);
    register(TOKEN_SEMICOLON,     null, null, PREC_NONE);
    register(TOKEN_SLASH_EQUAL,   null, null, PREC_NONE);
    register(TOKEN_STAR_EQUAL,    null, null, PREC_NONE);
    register(TOKEN_TYPE,          null, null, PREC_NONE);
    register(TOKEN_WHILE,         null, null, PREC_NONE);
  }
}
