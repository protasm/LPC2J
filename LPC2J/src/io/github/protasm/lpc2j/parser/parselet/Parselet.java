package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;

public interface Parselet {
    void parse(Parser parser, boolean canAssign, boolean inBinaryOp);
}
