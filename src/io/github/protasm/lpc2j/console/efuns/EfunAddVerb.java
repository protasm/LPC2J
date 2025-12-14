package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunAddVerb implements Efun {
        public static final EfunAddVerb INSTANCE = new EfunAddVerb();
        private static final Symbol SYM = new Symbol(LPCType.LPCVOID, "add_verb");

        private EfunAddVerb() {
        }

        @Override
        public Symbol symbol() {
                return SYM;
        }

        @Override
        public int arity() {
                return 1;
        }

        @Override
        public Object call(Object[] args) {
                // TODO: Implement verb registration when command handling is available
                return null;
        }
}
