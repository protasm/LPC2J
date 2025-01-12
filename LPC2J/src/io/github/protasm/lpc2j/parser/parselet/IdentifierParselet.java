package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.LPC2J;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.scanner.Token;

public class IdentifierParselet implements Parselet {
    @Override
    public void parse(Parser parser, LPC2J compiler, boolean canAssign, boolean inBinaryOp) {
	Token nameToken = compiler.parser().previous();
	String name = nameToken.lexeme();

	compiler.identifier(name, canAssign);
    }
}
