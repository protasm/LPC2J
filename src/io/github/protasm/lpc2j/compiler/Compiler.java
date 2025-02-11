package io.github.protasm.lpc2j.compiler;

import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.visitor.BytecodeVisitor;

public class Compiler {
    private final String defaultParentName;

    public Compiler(String defaultParentName) {
	this.defaultParentName = defaultParentName;
    }

    public byte[] compile(ASTObject astObject) {
	if (astObject == null)
	    return null;

	BytecodeVisitor bv = new BytecodeVisitor(defaultParentName);

	astObject.accept(bv);

	return bv.bytes();
    }
}
