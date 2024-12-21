package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.Parser;

public class VariableParselet implements Parselet {
  //parse(Parser, LPC2J, boolean)
  public void parse(Parser parser, LPC2J compiler, boolean canAssign) {
    compiler.namedVariable(parser.previous(), canAssign);
  }
}
