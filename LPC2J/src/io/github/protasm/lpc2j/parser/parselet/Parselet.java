package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.Parser;

public interface Parselet {

    void parse(Parser parser, LPC2J compiler, boolean canAssign);
}
