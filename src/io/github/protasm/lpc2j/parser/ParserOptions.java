package io.github.protasm.lpc2j.parser;

public class ParserOptions {
        private final boolean allowUntypedMethods;

        public ParserOptions() {
                this(false);
        }

        public ParserOptions(boolean allowUntypedMethods) {
                this.allowUntypedMethods = allowUntypedMethods;
        }

        public boolean allowUntypedMethods() {
                return allowUntypedMethods;
        }

        public static ParserOptions defaults() {
                return new ParserOptions(false);
        }
}
