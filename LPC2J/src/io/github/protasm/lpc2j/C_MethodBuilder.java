package io.github.protasm.lpc2j;

import java.util.ListIterator;
import java.util.Stack;

//import compiler.J.J_Method;
import io.github.protasm.lpc2j.scanner.Token;

public class C_MethodBuilder {
//  private J_Method method;
  private Stack<C_Local> locals; //simulates runtime stack
  private int scopeDepth;

  //C_MethodBuilder(J_Method)
  public C_MethodBuilder(J_Method method) {
	this.method = method;

    locals = new Stack<>();
    scopeDepth = 0;

    //Block out locals stack slot zero for method name
    Token token = new Token(null, method.name(), null, -1);

    locals.push(new C_Local(token, 0));
  } //C_Scope()

  //method()
  public J_Method method() {
    return this.method;
  } //method()

  //scopeDepth()
  public int scopeDepth() {
    return scopeDepth;
  } //scopeDepth()
  
  //incScopeDepth()
  public void incScopeDepth() {
    this.scopeDepth += 1;
  } //incScopeDepth()
  
  //decScopeDepth()
  public void decScopeDepth() {
    this.scopeDepth -= 1;
  } //decScopeDepth()

  //locals()
  public Stack<C_Local> locals() {
    return locals;
  } //locals()

  //addLocal(C_Local)
  public void addLocal(C_Local local) {
    locals.push(local);
  } //addLocal(C_Local)

  //hasLocal(Token)
  public boolean hasLocal(Token token) {
    ListIterator<C_Local> localsItr = locals.listIterator(locals.size());
    String name = token.lexeme();

    while (localsItr.hasPrevious()) {
      C_Local local = localsItr.previous();

      if (local.name().equals(name))
        return true;
    }

    return false;
  } //hasLocal(Token)

  //markTopLocalInitialized()
  public void markTopLocalInitialized(int scopeDepth) {
    locals.peek().setScopeDepth(scopeDepth);
  } //markTopLocalInitialized()
} //C_MethodBuilder