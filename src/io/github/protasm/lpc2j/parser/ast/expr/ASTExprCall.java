package io.github.protasm.lpc2j.parser.ast.expr;

import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.visitor.BytecodeVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprCall extends ASTExpression {
    private ASTMethod method;
    private ASTArguments arguments;

    public ASTExprCall(int line, ASTMethod method, ASTArguments arguments) {
	super(line);

	this.method = method;
	this.arguments = arguments;
    }

    public ASTMethod method() {
	return method;
    }

    public ASTArguments arguments() {
	return arguments;
    }

    @Override
    public LPCType lpcType() {
	return method.symbol().lpcType();
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
