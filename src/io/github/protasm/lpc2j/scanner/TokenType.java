package io.github.protasm.lpc2j.scanner;

import io.github.protasm.lpc2j.LPCType;

public enum TokenType {
	// Operators and punctuation
	T_BANG(Object.class), T_COLON(Object.class), T_COMMA(Object.class), T_DBL_AMP(Object.class),
	T_DBL_PIPE(Object.class), T_EQUAL(Object.class), T_LEFT_BRACE(Object.class), T_LEFT_BRACKET(Object.class),
	T_LEFT_PAREN(Object.class), T_MINUS(Object.class), T_MINUS_EQUAL(Object.class), T_MINUS_MINUS(Object.class),
	T_PLUS(Object.class), T_PLUS_EQUAL(Object.class), T_PLUS_PLUS(Object.class), T_RIGHT_ARROW(Object.class),
	T_RIGHT_BRACE(Object.class), T_RIGHT_BRACKET(Object.class), T_RIGHT_PAREN(Object.class), T_SEMICOLON(Object.class),
	T_SLASH(Object.class), T_SLASH_EQUAL(Object.class), T_STAR(Object.class), T_STAR_EQUAL(Object.class),

	// Comparison
	T_GREATER(Object.class), T_GREATER_EQUAL(Object.class), T_LESS(Object.class), T_LESS_EQUAL(Object.class),
	T_EQUAL_EQUAL(Object.class), T_BANG_EQUAL(Object.class),

	// All types words (int, string, etc.) are initially
	// scanned as T_TYPE with the LPCType stored as the
	// Token's literal.
	T_TYPE(LPCType.class),

	// Identifier and literals
	T_IDENTIFIER(String.class), T_INT_LITERAL(Integer.class), T_FLOAT_LITERAL(Float.class),
	T_STRING_LITERAL(String.class),

	// Control flow
	T_IF(String.class), T_ELSE(String.class), T_FOR(String.class), T_WHILE(String.class),

	// Reserved words
	T_INHERIT(String.class), T_FALSE(String.class), T_NIL(String.class), T_RETURN(String.class), T_SUPER(String.class),
	T_TRUE(String.class),

	// Synthetic
	T_EOF(Object.class), T_ERROR(Object.class);

	private final Class<?> clazz;

	TokenType(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Class<?> clazz() {
		return clazz;
	}
}
