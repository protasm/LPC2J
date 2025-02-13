package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.visitor.BytecodeVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.scanner.Token;

public class ASTExprLiteralInteger extends ASTExpression {
    private final Integer value;

    public ASTExprLiteralInteger(int line, Token<Integer> token) {
	super(line);

	this.value = token.literal();
    }

    public Integer value() {
	return value;
    }

    @Override
    public LPCType lpcType() {
	return LPCType.LPCINT;
    }

    @Override
    public void accept(BytecodeVisitor visitor) {
	visitor.visit(this);
    }

    @Override
    public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
	visitor.visit(this, lpcType);
    }

    @Override
    public void accept(PrintVisitor visitor) {
	visitor.visit(this);
    }
}
