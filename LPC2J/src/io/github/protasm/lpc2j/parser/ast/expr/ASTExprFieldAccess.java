package io.github.protasm.lpc2j.parser.ast.expr;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.LPCType;
import io.github.protasm.lpc2j.parser.ast.ASTField;

import static org.objectweb.asm.Opcodes.*;

public class ASTExprFieldAccess extends ASTExpression {
    private String parentName;
    private ASTField field;

    public ASTExprFieldAccess(int line, String parentName, ASTField field) {
	super(line);

	this.parentName = parentName;
	this.field = field;
    }
    
    public String parentName() {
	return parentName;
    }
    
    public ASTField field() {
	return field;
    }
    
    @Override
    public LPCType lpcType() {
	return field.lpcType();
    }

    @Override
    public void toBytecode(MethodVisitor mv) {
	    mv.visitVarInsn(ALOAD, 0);
	    mv.visitFieldInsn(GETFIELD, parentName, field.name(), field.descriptor());
	}

    @Override
    public String toString() {
	return className();
    }
}
