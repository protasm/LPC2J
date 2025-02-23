package io.github.protasm.lpc2j.parser.ast.expr;

import java.lang.reflect.Method;

import io.github.protasm.lpc2j.compiler.Compiler;
import io.github.protasm.lpc2j.compiler.GfunsIntfc;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.type.LPCType;

public class ASTExprCallGfun extends ASTExpression {
    private final GfunsIntfc gfuns;
    private final Method gfun;
    private final ASTArguments arguments;

    public ASTExprCallGfun(int line, GfunsIntfc gfuns, Method gfun, ASTArguments arguments) {
	super(line);

	this.gfuns = gfuns;
	this.gfun = gfun;
	this.arguments = arguments;
    }

    public GfunsIntfc gfuns() {
	return gfuns;
    }

    public Method gfun() {
	return gfun;
    }

    public ASTArguments arguments() {
	return arguments;
    }

    @Override
    public LPCType lpcType() {
	return LPCType.LPCMIXED;
    }

    @Override
    public void accept(Compiler visitor) {
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
