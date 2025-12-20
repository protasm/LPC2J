package io.github.protasm.lpc2j.token;

import io.github.protasm.lpc2j.sourcepos.SourceSpan;

public record Token<T>(TokenType type, String lexeme, T literal, SourceSpan span) {
    public Token {
        if ((literal != null) && (type != null) && !type.clazz().isInstance(literal))
            throw new IllegalArgumentException(
                    String.format("literal %s is not an instance of %s", literal.getClass(), type.clazz()));
    }

    public int length() {
        return (lexeme != null) ? lexeme.length() : 0;
    }

    @SuppressWarnings("unchecked")
    public Class<T> literalType() {
        return literal != null ? (Class<T>) literal.getClass() : (Class<T>) Object.class;
    }

    @Override
    public String toString() {
        if (type == TokenType.T_EOF)
            return type.toString();

        if (literal == null)
            return type + "(" + lexeme + ")";

        return type + "<" + literal.getClass().getSimpleName() + ">(" + lexeme + ")";
    }

    public int line() {
        return (span != null) ? span.startLine() : -1;
    }
}
