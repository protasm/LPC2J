package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;

public class InfixInvoke implements InfixParselet {
    @Override
    public ASTExpression parse(Parser parser, ASTExpression left, boolean canAssign) {
//	compiler.argumentList();

	// compiler.call()
	return null; // TODO
    }
}
