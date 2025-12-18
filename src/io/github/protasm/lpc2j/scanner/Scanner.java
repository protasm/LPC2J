package io.github.protasm.lpc2j.scanner;

import static io.github.protasm.lpc2j.token.TokenType.T_BANG;
import static io.github.protasm.lpc2j.token.TokenType.T_BANG_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_COLON;
import static io.github.protasm.lpc2j.token.TokenType.T_COMMA;
import static io.github.protasm.lpc2j.token.TokenType.T_DBL_AMP;
import static io.github.protasm.lpc2j.token.TokenType.T_DBL_PIPE;
import static io.github.protasm.lpc2j.token.TokenType.T_ELSE;
import static io.github.protasm.lpc2j.token.TokenType.T_EOF;
import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_ERROR;
import static io.github.protasm.lpc2j.token.TokenType.T_FALSE;
import static io.github.protasm.lpc2j.token.TokenType.T_FLOAT_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_FOR;
import static io.github.protasm.lpc2j.token.TokenType.T_GREATER;
import static io.github.protasm.lpc2j.token.TokenType.T_GREATER_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.token.TokenType.T_IF;
import static io.github.protasm.lpc2j.token.TokenType.T_INHERIT;
import static io.github.protasm.lpc2j.token.TokenType.T_INT_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_BRACE;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_BRACKET;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.lpc2j.token.TokenType.T_LESS;
import static io.github.protasm.lpc2j.token.TokenType.T_LESS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS_MINUS;
import static io.github.protasm.lpc2j.token.TokenType.T_NIL;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS_PLUS;
import static io.github.protasm.lpc2j.token.TokenType.T_RETURN;
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
import static io.github.protasm.lpc2j.token.TokenType.T_TRUE;
import static io.github.protasm.lpc2j.token.TokenType.T_TYPE;
import static io.github.protasm.lpc2j.token.TokenType.T_WHILE;

import java.util.HashMap;
import java.util.Map;

import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.preproc.Preprocessor;
import io.github.protasm.lpc2j.sourcepos.SourcePos;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenList;
import io.github.protasm.lpc2j.token.TokenType;

public class Scanner {
    private static final char EOL = '\n';
    private static final Map<String, LPCType> lpcTypeWords;
    private static final Map<String, TokenType> reservedWords;
    private static final Map<Character, TokenType> oneCharLexemes;
    private ScannableSource ss;

    static {
        lpcTypeWords = new HashMap<>() {
            private static final long serialVersionUID = 1L;

            {
                put("int", LPCType.LPCINT);
                put("float", LPCType.LPCFLOAT);
                put("mapping", LPCType.LPCMAPPING);
                put("mixed", LPCType.LPCMIXED);
                put("object", LPCType.LPCOBJECT);
                put("status", LPCType.LPCSTATUS);
                put("string", LPCType.LPCSTRING);
                put("void", LPCType.LPCVOID);
            }
        };

        reservedWords = new HashMap<>() {
            private static final long serialVersionUID = 1L;

            {
                put("else", T_ELSE);
                put("false", T_FALSE);
                put("for", T_FOR);
                put("if", T_IF);
                put("inherit", T_INHERIT);
                put("nil", T_NIL);
                put("return", T_RETURN);
                put("true", T_TRUE);
                put("while", T_WHILE);
            }
        };

        oneCharLexemes = new HashMap<>() {
            private static final long serialVersionUID = 1L;

            {
                put('(', T_LEFT_PAREN);
                put(')', T_RIGHT_PAREN);
                put('{', T_LEFT_BRACE);
                put('}', T_RIGHT_BRACE);
                put('[', T_LEFT_BRACKET);
                put(']', T_RIGHT_BRACKET);
                put(',', T_COMMA);
                put(';', T_SEMICOLON);
            }
        };
    }

    public TokenList scan(String source) {
        if (source == null)
            throw new ScanException("Source text cannot be null.", -1);

        try {
            String processed = Preprocessor.preprocess(source).source;

            ss = new ScannableSource(processed);

            TokenList tokens = new TokenList();
            Token<?> token;

            do {
                token = lexToken();

                if (token != null)
                    tokens.add(token);
            } while ((token == null) || (token.type() != T_EOF));

            return tokens;
        } catch (ScanException e) {
            throw e;
        } catch (RuntimeException e) {
            int line = (ss != null) ? ss.pos().line() : -1;

            throw new ScanException("Failed to scan source: " + e.getMessage(), line, e);
        }
    }

    private Token<?> lexToken() {

        if (ss.atEnd())
            return token(T_EOF);

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

        String str = ss.read();

        if (lpcTypeWords.keySet().contains(str))
            return typeToken(str);

        TokenType type = reservedWords.get(str);

        if (type == null)
            type = T_IDENTIFIER;

        return token(type);
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

        SourcePos pos = ss.pos();

        try {
            if (isFloat)
                return floatToken(T_FLOAT_LITERAL, lexeme, Float.parseFloat(lexeme));
            else
                return intToken(T_INT_LITERAL, lexeme, Integer.parseInt(lexeme));
        } catch (NumberFormatException e) {
            throw new ScanException("Invalid numeric literal: '" + lexeme + "'", pos.line(), e);
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
        SourcePos pos = ss.pos();
        return new Token<>(type, ss.read(), null, pos.line());
    }

    private Token<String> errorToken(String message) {
        SourcePos pos = ss.pos();
        return new Token<>(T_ERROR, message, null, pos.line());
    }

    private Token<Integer> intToken(TokenType type, String lexeme, Integer i) {
        SourcePos pos = ss.pos();
        return new Token<>(type, lexeme, i, pos.line());
    }

    private Token<Float> floatToken(TokenType type, String lexeme, Float f) {
        SourcePos pos = ss.pos();
        return new Token<>(type, lexeme, f, pos.line());
    }

    private Token<String> stringToken(TokenType type, String literal) {
        SourcePos pos = ss.pos();
        return new Token<>(type, ss.read(), literal, pos.line());
    }

    private Token<LPCType> typeToken(String lexeme) {
        LPCType type = lpcTypeWords.get(lexeme);
        SourcePos pos = ss.pos();

        return new Token<>(T_TYPE, lexeme, type, pos.line());
    }
}
