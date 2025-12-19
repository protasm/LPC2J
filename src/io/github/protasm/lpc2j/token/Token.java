package io.github.protasm.lpc2j.token;

public record Token<T>(TokenType type, String lexeme, T literal, int line) {
    public Token() {
        this(null, null, null, -1);
    }

    public Token(TokenType tType) {
        this(tType, null, null, -1);
    }

    public Token(String lexeme) {
        this(null, lexeme, null, -1);
    }

    public int length() {
        return lexeme.length();
    }

    @SuppressWarnings("unchecked")
    public Class<T> literalType() {
        return (Class<T>) literal.getClass();
    }

    @Override
    public String toString() {
        if (type == TokenType.T_EOF)
            return type.toString();
        if (literal == null)
            return type + "(" + lexeme + ")";
        else
            return type + "<" + literal.getClass().getSimpleName() + ">(" + lexeme + ")";
    }
}
