package io.github.protasm.lpc2j.runtime;

/** Describes the runtime shape for LPC values after semantic typing. */
public enum RuntimeValueKind {
    INT,
    FLOAT,
    STATUS,
    STRING,
    OBJECT,
    MAPPING,
    MIXED,
    ARRAY,
    EFUN,
    NULL,
    VOID
}
