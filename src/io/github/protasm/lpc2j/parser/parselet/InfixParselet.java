package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;

public interface InfixParselet {
    ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign);
}
