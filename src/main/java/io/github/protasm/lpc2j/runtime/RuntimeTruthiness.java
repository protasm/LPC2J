package io.github.protasm.lpc2j.runtime;

/** Captures how a runtime value should be interpreted in conditional contexts. */
public enum RuntimeTruthiness {
    /** Numeric zero is false; non-zero is true. */
    NUMERIC_ZERO_FALSE,

    /** {@code null} is false; all other references are true. */
    REFERENCE_NULL_FALSE,

    /** Value is always considered true (used for control constructs, rarely practical). */
    ALWAYS_TRUE,

    /** Value is always considered false. */
    ALWAYS_FALSE,

    /** Truthiness is not applicable (e.g., {@code void}). */
    NONE
}
