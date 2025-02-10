package io.github.protasm.lpc2j.parser.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.MethodVisitor;

import io.github.protasm.lpc2j.parser.LPCType;
import io.github.protasm.lpc2j.parser.ast.visitor.PrintVisitor;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ASTArguments extends ASTNode implements Iterable<ASTArgument> {
	private final List<ASTArgument> arguments;

	public ASTArguments(int line) {
		super(line);

		this.arguments = new ArrayList<>();
	}

	public void add(ASTArgument argument) {
		arguments.add(argument);
	}

	public ASTArgument get(int i) {
		return arguments.get(i);
	}

	public int size() {
		return arguments.size();
	}

	@Override
	public Iterator<ASTArgument> iterator() {
		return arguments.iterator();
	}

	@Override
	public void accept(MethodVisitor mv) {
		mv.visitLdcInsn(arguments.size()); // Push array length

		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // Create Object[]

		for (int i = 0; i < arguments.size(); i++) {
			mv.visitInsn(DUP); // Duplicate array reference

			mv.visitLdcInsn(i); // Push index

			arguments.get(i).accept(mv); // Push argument value

			// If argument is a primitive, box it (e.g., int -> Integer)
//            boxIfPrimitive(mv, arguments.get(i));

			mv.visitInsn(AASTORE); // Store argument into array
		}
	}

	@Override
	public void accept(TypeInferenceVisitor visitor, LPCType lpcType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void accept(PrintVisitor visitor) {
		visitor.visit(this);
	}
}
