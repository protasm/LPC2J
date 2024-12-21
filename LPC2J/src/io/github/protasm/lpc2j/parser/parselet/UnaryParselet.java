package io.github.protasm.lpc2j.parser.parselet;

import static parser.Parser.Precedence.PREC_UNARY;

import compiler.C_Compiler;
import compiler.J.J_OpCode;
import parser.Parser;
import scanner.TokenType;

public class UnaryParselet implements Parselet {
  //parse(Parser, C_Compiler, boolean)
  public void parse(Parser parser, C_Compiler compiler, boolean canAssign) {
    TokenType operatorType = parser.previous().type();

    // Compile the single operand.
    parser.parsePrecedence(PREC_UNARY);

    // Emit the operator instruction.
    switch (operatorType) {
      case TOKEN_BANG:
//        compiler.not();

        break;
      case TOKEN_MINUS:
        
        break;
      default: //Unreachable
        return;
    } //switch
  }
} //UnaryParselet