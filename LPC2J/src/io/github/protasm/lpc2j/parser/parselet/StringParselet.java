package io.github.protasm.lpc2j.parser.parselet;

import compiler.C_Compiler;
import parser.Parser;

public class StringParselet implements Parselet {
  //parse(Parser, C_Compiler, boolean)
  public void parse(Parser parser, C_Compiler compiler, boolean canAssign) {
    String value = (String) parser.previous().literal();
    
    compiler.lpcString(value);
  }
}
