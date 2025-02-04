package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public interface PrefixParselet {
    ASTExpression parse(Parser parser, boolean canAssign);
}
