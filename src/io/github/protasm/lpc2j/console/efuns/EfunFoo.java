package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunFoo implements Efun {
	public static final EfunFoo INSTANCE = new EfunFoo();
	private static final Symbol SYM = new Symbol(LPCType.LPCVOID, "foo");

	private EfunFoo() {
	}

	@Override
	public Symbol symbol() {
		return SYM;
	}

	@Override
	public int arity() {
		return 0;
	}

	@Override
	public Object call(Object[] args) {
		System.out.print("foo");

		return null;
	}
}
