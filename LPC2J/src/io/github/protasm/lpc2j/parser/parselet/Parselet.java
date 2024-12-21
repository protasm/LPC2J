package io.github.protasm.lpc2j.parser.parselet;

import compiler.C_Compiler;
import parser.Parser;

public interface Parselet {
  //parse(Parser, C_Compiler, boolean);
  void parse(Parser parser, C_Compiler compiler, boolean canAssign);
}
