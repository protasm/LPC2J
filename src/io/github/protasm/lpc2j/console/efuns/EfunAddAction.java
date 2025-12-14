package io.github.protasm.lpc2j.console.efuns;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.type.LPCType;

public final class EfunAddAction implements Efun {
        public static final EfunAddAction INSTANCE = new EfunAddAction();
        private static final Symbol SYM = new Symbol(LPCType.LPCVOID, "add_action");

        private EfunAddAction() {
        }

        @Override
        public Symbol symbol() {
                return SYM;
        }

        @Override
        public int arity() {
                return 2;
        }

        @Override
        public Object call(Object[] args) {
                // TODO: Implement action registration when command handling is available
                return null;
        }
}
