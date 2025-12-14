package io.github.protasm.lpc2j.parser;

public class ParserOptions {
        private final boolean requireUntyped;

        public ParserOptions() {
                this(false);
        }

        public ParserOptions(boolean requireUntyped) {
                this.requireUntyped = requireUntyped;
        }

        public boolean requireUntyped() {
                return requireUntyped;
        }

        public static ParserOptions defaults() {
                return new ParserOptions(false);
        }
}
