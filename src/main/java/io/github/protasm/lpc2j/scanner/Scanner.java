package io.github.protasm.lpc2j.scanner;

import static io.github.protasm.lpc2j.token.TokenType.T_BANG;
import static io.github.protasm.lpc2j.token.TokenType.T_BANG_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_COLON;
import static io.github.protasm.lpc2j.token.TokenType.T_COMMA;
import static io.github.protasm.lpc2j.token.TokenType.T_DBL_AMP;
import static io.github.protasm.lpc2j.token.TokenType.T_DBL_PIPE;
import static io.github.protasm.lpc2j.token.TokenType.T_EOF;
import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_ERROR;
import static io.github.protasm.lpc2j.token.TokenType.T_FLOAT_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_GREATER;
import static io.github.protasm.lpc2j.token.TokenType.T_GREATER_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.token.TokenType.T_INT_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_BRACE;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_BRACKET;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.lpc2j.token.TokenType.T_LESS;
import static io.github.protasm.lpc2j.token.TokenType.T_LESS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS_MINUS;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS_PLUS;
import static io.github.protasm.lpc2j.token.TokenType.T_QUESTION;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_ARROW;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_BRACE;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_BRACKET;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_PAREN;
import static io.github.protasm.lpc2j.token.TokenType.T_SEMICOLON;
import static io.github.protasm.lpc2j.token.TokenType.T_SLASH;
import static io.github.protasm.lpc2j.token.TokenType.T_SLASH_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_STAR;
import static io.github.protasm.lpc2j.token.TokenType.T_STAR_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_STRING_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_SUPER;

import java.nio.file.Path;
import java.util.Map;
import io.github.protasm.lpc2j.sourcepos.SourceSpan;
import io.github.protasm.lpc2j.preproc.PreprocessException;
import io.github.protasm.lpc2j.preproc.PreprocessedSource;
import io.github.protasm.lpc2j.preproc.Preprocessor;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenList;
import io.github.protasm.lpc2j.token.TokenType;

public class Scanner {
    private static final char EOL = '\n';
    private ScannableSource ss;
    private final Preprocessor preprocessor;

    private static final Map<Character, TokenType> oneCharLexemes =
            Map.of(
                    '(', T_LEFT_PAREN,
                    ')', T_RIGHT_PAREN,
                    '{', T_LEFT_BRACE,
                    '}', T_RIGHT_BRACE,
                    '[', T_LEFT_BRACKET,
                    ']', T_RIGHT_BRACKET,
                    ',', T_COMMA,
                    ';', T_SEMICOLON,
                    '?', T_QUESTION);

    public Scanner() {
        this(new Preprocessor(Preprocessor.rejectingResolver()));
    }

    public Scanner(Preprocessor preprocessor) {
        this.preprocessor = preprocessor;
    }

    public TokenList scan(String source) {
        return scan(null, source);
    }

    public TokenList scan(Path sourcePath, String source) {
        return scan(sourcePath, source, null);
    }

    public TokenList scan(Path sourcePath, String source, String displayPath) {
        if (source == null)
            throw new ScanException("Source text cannot be null.", -1);

        try {
            PreprocessedSource processed = preprocessor.preprocessWithMapping(sourcePath, source, displayPath);

            ss = new ScannableSource(processed);

            TokenList tokens = new TokenList();
            Token<?> token;

            do {
                token = lexToken();

                if (token != null)
                    tokens.add(token);
            } while ((token == null) || (token.type() != T_EOF));

            return mergeStringLiterals(tokens);
        } catch (PreprocessException e) {
            throw new ScanException("Failed to scan source: " + e.getMessage(), e.getLine(), e);
        } catch (ScanException e) {
            throw e;
        } catch (RuntimeException e) {
            int line = (ss != null) ? ss.pos().line() : -1;

            throw new ScanException("Failed to scan source: " + e.getMessage(), line, e);
        }
    }

    private TokenList mergeStringLiterals(TokenList tokens) {
        TokenList merged = new TokenList();

        for (int i = 0; i < tokens.size(); i++) {
            Token<?> token = tokens.get(i);

            if (token.type() != T_STRING_LITERAL) {
                merged.add(token);
                continue;
            }

            StringBuilder lexeme = new StringBuilder(token.lexeme());
            StringBuilder literal = new StringBuilder(token.literal().toString());
            SourceSpan span = token.span();

            int j = i + 1;
            while (j < tokens.size() && tokens.get(j).type() == T_STRING_LITERAL) {
                Token<?> next = tokens.get(j);
                lexeme.append(next.lexeme());
                literal.append(next.literal().toString());
                span = (span != null && next.span() != null) ? SourceSpan.encompassing(span, next.span()) : span;
                j++;
            }

            merged.add(new Token<>(T_STRING_LITERAL, lexeme.toString(), literal.toString(), span));
            i = j - 1;
        }

        return merged;
    }

    private Token<?> lexToken() {

        if (ss.atEnd()) {
            ss.syncTailHead();
            return token(T_EOF);
        }

        ss.syncTailHead();

        char c = ss.consumeOneChar();

        if (oneCharLexemes.containsKey(c))
            return token(oneCharLexemes.get(c));

        if (isDigit(c))
            return number();

        if (isAlpha(c))
            return identifier();

        switch (c) {
        case EOL:
            return null;
        case '"':
            return stringLiteral();
        case '&':
            if (ss.match('&'))
                return token(T_DBL_AMP);
            else
                return unexpectedChar(c);
        case '|':
            if (ss.match('|'))
                return token(T_DBL_PIPE);
            else
                return unexpectedChar(c);
        case ':':
            if (ss.match(':'))
                return token(T_SUPER);
            else
                return token(T_COLON);
        case '-':
            if (ss.match('-'))
                return token(T_MINUS_MINUS);
            else if (ss.match('='))
                return token(T_MINUS_EQUAL);
            else if (ss.match('>'))
                return token(T_RIGHT_ARROW);
            else
                return token(T_MINUS);
        case '+':
            if (ss.match('+'))
                return token(T_PLUS_PLUS);
            else if (ss.match('='))
                return token(T_PLUS_EQUAL);
            else
                return token(T_PLUS);
        case '!':
            return token(ss.match('=') ? T_BANG_EQUAL : T_BANG);
        case '=':
            return token(ss.match('=') ? T_EQUAL_EQUAL : T_EQUAL);
        case '<':
            return token(ss.match('=') ? T_LESS_EQUAL : T_LESS);
        case '>':
            return token(ss.match('=') ? T_GREATER_EQUAL : T_GREATER);
        case '/':
            if (ss.match('/'))
                return lineComment();
            else if (ss.match('*'))
                return blockComment();
            else if (ss.match('='))
                return token(T_SLASH_EQUAL);
            else
                return token(T_SLASH);
        case '*':
            return token(ss.match('=') ? T_STAR_EQUAL : T_STAR);
        case ' ':
        case '\r':
        case '\t':
            while (isWhitespace(ss.peek()))
                ss.advance();

            return null;
        default:
            return unexpectedChar(c);
        }
    }

    private Token<?> lineComment() {
        ss.advanceTo(EOL);

        return null;
    }

    private Token<?> blockComment() {
        while (!ss.atEnd()) {
            ss.advanceTo('*');

            if (ss.peekPrev() == '/')
                return errorToken("Nested block comment");

            ss.advance();

            if (ss.match('/'))
                return null;
        }

        return errorToken("Unterminated block comment.");
    }

    private Token<?> identifier() {
        while (isAlphaNumeric(ss.peek()))
            ss.advance();

        return token(T_IDENTIFIER);
    }

    private Token<?> number() {
        boolean isFloat = false;

        while (isDigit(ss.peek()))
            ss.advance();

        if ((ss.peek() == '.') && isDigit(ss.peekNext())) {
            isFloat = true;

            ss.advance();

            while (isDigit(ss.peek()))
                ss.advance();
        }

        String lexeme = ss.read();

        try {
            if (isFloat)
                return floatToken(T_FLOAT_LITERAL, lexeme, Float.parseFloat(lexeme));
            else
                return intToken(T_INT_LITERAL, lexeme, Integer.parseInt(lexeme));
        } catch (NumberFormatException e) {
            return errorToken("Invalid numeric literal: '" + lexeme + "'");
        }
    }

    private Token<String> stringLiteral() {
        if (!ss.advanceTo('"'))
            return errorToken("Unterminated string.");

        ss.advance();

        return stringToken(T_STRING_LITERAL, ss.readTrimmed());
    }

    private boolean isWhitespace(char c) {
        return (c == ' ') || (c == '\r') || (c == '\t');
    }

    private boolean isAlpha(char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || (c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return (c >= '0') && (c <= '9');
    }

    private Token<String> unexpectedChar(char c) {
        return errorToken("Unexpected character: '" + c + "'.");
    }

    private Token<Object> token(TokenType type) {
        return new Token<>(type, ss.read(), null, ss.span());
    }

    private Token<String> errorToken(String message) {
        return new Token<>(T_ERROR, message, null, ss.span());
    }

    private Token<Integer> intToken(TokenType type, String lexeme, Integer i) {
        return new Token<>(type, lexeme, i, ss.span());
    }

    private Token<Float> floatToken(TokenType type, String lexeme, Float f) {
        return new Token<>(type, lexeme, f, ss.span());
    }

    private Token<String> stringToken(TokenType type, String literal) {
        return new Token<>(type, ss.read(), literal, ss.span());
    }

}
