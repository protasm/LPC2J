package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.ParseRule;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.TokenType;

public class BinaryParselet implements Parselet {
  //parse(Parser, C_Compiler, boolean)
  public void parse(Parser parser, LPC2J compiler, boolean canAssign) {
    TokenType operatorType = parser.previous().type();
    ParseRule rule = parser.getRule(operatorType);

    //compile the second operand
    parser.parsePrecedence(rule.precedence() + 1);

    switch (operatorType) {
//      case TOKEN_BANG_EQUAL:
//        compiler.emitCode(OP_EQUAL);
//        compiler.emitCode(OP_NOT);
//
//        break;
//      case TOKEN_EQUAL_EQUAL:
//        compiler.emitCode(OP_EQUAL);
//
//        break;
//      case TOKEN_GREATER:
//        compiler.emitCode(OP_GREATER);
//
//        break;
//      case TOKEN_GREATER_EQUAL:
//        compiler.emitCode(OP_LESS);
//        compiler.emitCode(OP_NOT);
//
//        break;
//      case TOKEN_LESS:
//        compiler.emitCode(OP_LESS);
//
//        break;
//      case TOKEN_LESS_EQUAL:
//        compiler.emitCode(OP_GREATER);
//        compiler.emitCode(OP_NOT);
//
//        break;
//      case TOKEN_PLUS:
//        compiler.addition()
//
//        break;
      case TOKEN_MINUS:
        

        break;
//      case TOKEN_STAR:
//        compiler.multiplication();
//
//        break;
//      case TOKEN_SLASH:
//        compiler.division();
//
//        break;
//      case TOKEN_PLUS_EQUAL:
//        break;
      default:  //Unreachable
        return;
    }
  }
} //BinaryParselet