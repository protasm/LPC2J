package io.github.protasm.lpc2j;

import io.github.protasm.lpc2j.scanner.Token;

public class C_Local {
  private Token token;
  private int scopeDepth; //-1 if not yet initialized
  private boolean isCaptured;

  //C_Local()
  public C_Local() {
    this(null, -1);
  }

  //C_Local(Token, int)
  public C_Local(Token token, int scopeDepth) {
    this.token = token;
    this.scopeDepth = scopeDepth;

    isCaptured = false;
  }

  //token()
  public Token token() {
    return token;
  }

  //name()
  public String name() {
    return token.lexeme();
  }

  //scopeDepth()
  public int scopeDepth() {
    return scopeDepth;
  }

  //setScopeDepth(int)
  public void setScopeDepth(int scopeDepth) {
    this.scopeDepth = scopeDepth;
  }

  //isCaptured()
  public boolean isCaptured() {
    return isCaptured;
  }

  //setIsCaptured(boolean)
  public void setIsCaptured(boolean isCaptured) {
    this.isCaptured = isCaptured;
  }

  //toString()
  @Override
  public String toString() {
    return "[ " + token.lexeme() + " (" + scopeDepth + ") ]";
  }
}
