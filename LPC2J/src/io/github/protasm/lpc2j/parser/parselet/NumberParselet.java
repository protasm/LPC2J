package io.github.protasm.lpc2j.parser.parselet;

import compiler.C_Compiler;
import parser.Parser;
import scanner.TokenType;

public class NumberParselet implements Parselet {
  //parse(Parser, C_Compiler, boolean)
  public void parse(Parser parser, C_Compiler compiler, boolean canAssign) {
	Object literal = parser.previous().literal();
    TokenType numType = parser.previous().type();
    
    switch(numType) {
      case TOKEN_NUM_INT:
    	compiler.lpcInteger((Integer) literal);
    	
        return;
      case TOKEN_NUM_FLOAT:
    	compiler.lpcFloat((Float) literal);
    	
        return;
      default: //Unreachable
        break;
    }

    compiler.invalidNumber(literal);
  } //parse(Parser, C_Compiler, boolean)
} //NumberParselet