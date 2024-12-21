package io.github.protasm.lpc2j.parser;

import io.github.protasm.lpc2j.parser.parselet.Parselet;

public class ParseRule {
  //the function to compile a prefix expression that starts with a token of a
  //particular type that is associated with this ParseRule object in Compiler;
  private Parselet prefix;

  //the function to compile an infix expression whose left operand is followed by
  //that same associated token of that type;
  private Parselet infix;

  //the precedence of an infix expression that uses that token as an operator.
  private int precedence;

  //NB:  It is not necessary to track the precedence of a prefix expression
  //starting with a given token because all prefix operators have the same
  //precedence.

  //ParseRule(Parselet, Parselet, int)
  public ParseRule(Parselet prefix, Parselet infix, int precedence) {
    this.prefix = prefix;
    this.infix = infix;
    this.precedence = precedence;
  }

  //prefix()
  public Parselet prefix() {
    return prefix;
  }

  //infix()
  public Parselet infix() {
    return infix;
  }

  //precedence()
  public int precedence() {
    return precedence;
  }
}
