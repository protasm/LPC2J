package io.github.protasm.lpc2j.parser;

import static io.github.protasm.lpc2j.token.TokenType.T_COMMA;
import static io.github.protasm.lpc2j.token.TokenType.T_ELSE;
import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.token.TokenType.T_IF;
import static io.github.protasm.lpc2j.token.TokenType.T_INHERIT;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_BRACE;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_BRACKET;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_RETURN;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_BRACE;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_BRACKET;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_PAREN;
import static io.github.protasm.lpc2j.token.TokenType.T_SEMICOLON;
import static io.github.protasm.lpc2j.token.TokenType.T_STRING_LITERAL;
import static io.github.protasm.lpc2j.token.TokenType.T_TYPE;

import java.util.ArrayList;
import java.util.List;

import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprNull;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor;
import io.github.protasm.lpc2j.parser.parselet.InfixParselet;
import io.github.protasm.lpc2j.parser.parselet.PrefixParselet;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.sourcepos.SourceSpan;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenClassifier;
import io.github.protasm.lpc2j.token.TokenType;
import io.github.protasm.lpc2j.token.TokenList;

public class Parser {
        private TokenList tokens;
        private ASTObject currObj;
        private Locals locals;
        private LPCType currentReturnType;
        private final ParserOptions options;

        public Parser() {
                this(ParserOptions.defaults());
        }

        public Parser(ParserOptions options) {
                this.options = (options == null) ? ParserOptions.defaults() : options;
        }

    public TokenList tokens() {
        return this.tokens;
    }

    public ASTObject currObj() {
        return this.currObj;
    }

    public Locals locals() {
        return this.locals;
    }

    public int currLine() {
        return tokens.current().line();
    }

    public ASTObject parse(String objName, TokenList tokens) {
        if (tokens == null)
            throw new ParseException("Token list cannot be null.");

        this.tokens = TokenClassifier.classify(tokens);

        try {
            currObj = new ASTObject(0, objName);

            declarations(); // pass 1

            definitions(); // pass 2

            typeInference(); // pass 3

            return currObj;
        } catch (ParseException e) {
            throw e;
        } catch (RuntimeException e) {
            Token<?> current = (this.tokens != null) ? this.tokens.current() : null;

            if (current != null)
                throw new ParseException("Unexpected parser failure: " + e.getMessage(), current, e);

            throw new ParseException("Unexpected parser failure: " + e.getMessage(), -1, e);
        }
    }

    private void declarations() {
        skipInherit();

        while (!tokens.isAtEnd())
            property(false);
    }

    private void definitions() {
        tokens.reset();

        currObj.setParentName(inherit());

        while (!tokens.isAtEnd())
            property(true);
    }

    private void typeInference() {
        currObj.accept(new TypeInferenceVisitor(), LPCType.LPCNULL);
    }

    private String inherit() {
        if (!tokens.match(T_INHERIT))
            return null;

        Token<String> parentToken = tokens.consume(T_STRING_LITERAL, "Expect string after 'inherit'.");

        tokens.consume(T_SEMICOLON, "Expect ';' after inherited object path.");

        return parentToken.lexeme();
    }

    private void property(boolean define) {
                Symbol symbol = declarationSymbol();

                if (tokens.match(T_LEFT_PAREN))
                        method(symbol, define);
                else
                        field(symbol, define);
        }

        private Symbol declarationSymbol() {
                if (tokens.check(T_TYPE)) {
                        Token<LPCType> typeToken = tokens.consume(T_TYPE, "Expect property type.");
                        Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect property name.");
                        Symbol symbol = new Symbol(typeToken, nameToken);

                        return symbol;
                }

                throw new ParseException("Expect property type.", tokens.current());
        }

    private void field(Symbol symbol, boolean define) {
        if (define && locals == null)
            locals = new Locals();

        List<FieldDeclarator> declarators = fieldDeclarators(symbol, define);

        if (!define) {
            for (FieldDeclarator declarator : declarators) {
                ASTField field = new ASTField(currLine(), currObj.name(), declarator.symbol());

                currObj.fields().put(field.symbol().name(), field);
            }

            return;
        }

        for (FieldDeclarator declarator : declarators) {
            ASTField field = currObj.fields().get(declarator.symbol().name());

            if (field == null)
                throw new ParseException("Unrecognized field '" + declarator.symbol().name() + "'.", tokens.current());

            field.setInitializer(declarator.initializer());
        }
    }

    private List<FieldDeclarator> fieldDeclarators(Symbol symbol, boolean define) {
        List<FieldDeclarator> declarators = new ArrayList<>();

        declarators.add(fieldDeclarator(symbol, define));

        while (tokens.match(T_COMMA)) {
            Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect field name.");
            Symbol additionalSymbol = new Symbol(symbol.declaredType(), nameToken.lexeme());

            declarators.add(fieldDeclarator(additionalSymbol, define));
        }

        tokens.consume(T_SEMICOLON, "Expect ';' after field declaration.");

        return declarators;
    }

    private FieldDeclarator fieldDeclarator(Symbol symbol, boolean define) {
        ASTExpression initializer = null;

        if (tokens.match(T_EQUAL)) {
            if (define)
                initializer = expression();
            else
                skipInitializer();
        }

        return new FieldDeclarator(symbol, initializer);
    }

        private void method(Symbol symbol, boolean define) {
                if (!define) {
                        skipMethodBody();

                        ASTMethod method = new ASTMethod(currLine(), currObj.name(), symbol);

            currObj.methods().put(method.symbol().name(), method);

            return;
        }

                ASTMethod method = currObj.methods().get(symbol.name());

                locals = new Locals();
                currentReturnType = symbol.lpcType();

                method.setParameters(parameters());

                tokens.consume(T_LEFT_BRACE, "Expect '{' after method declaration.");

                method.setBody(block(true));
        }

    private void skipInherit() {
        if (!tokens.match(T_INHERIT))
            return;

        tokens.advanceThrough(T_SEMICOLON);
    }

    private void skipMethodBody() {
        int count = 0;

        while (!tokens.isAtEnd())
            if (tokens.match(T_LEFT_BRACE))
                count++;
            else if (tokens.match(T_RIGHT_BRACE)) {
                count--;

                if (count == 0)
                    return;
            } else
                tokens.advance();

                throw new ParseException("Unmatched '{' in method body.", tokens.current());
    }

        private ASTParameters parameters() {
                ASTParameters params = new ASTParameters(currLine());

                if (tokens.match(T_RIGHT_PAREN)) // No parameters
                        return params;

                do {
                        Token<LPCType> typeToken = tokens.consume(T_TYPE, "Expect parameter type.");
                        Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect parameter name.");
                        Symbol symbol = new Symbol(typeToken, nameToken);

                        ASTParameter param = new ASTParameter(currLine(), symbol);
                        ASTLocal local = new ASTLocal(currLine(), symbol);

                        params.add(param);

            locals.add(local, true);
        } while (tokens.match(T_COMMA));

        tokens.consume(T_RIGHT_PAREN, "Expect ')' after method parameters.");

        return params;
    }

    public ASTArguments arguments() {
        ASTArguments args = new ASTArguments(currLine());

        tokens.consume(T_LEFT_PAREN, "Expect '(' after method name.");

        if (tokens.match(T_RIGHT_PAREN)) // No arguments
            return args;

        do {
            ASTExpression expr = expression();
            ASTArgument arg = new ASTArgument(currLine(), expr);

            args.add(arg);
        } while (tokens.match(T_COMMA));

        tokens.consume(T_RIGHT_PAREN, "Expect ')' after method arguments.");

        return args;
    }

        private ASTStmtBlock block(boolean isMethodBody) {
                locals.beginScope();

                List<ASTStatement> statements = new ArrayList<>();

                while (!tokens.check(T_RIGHT_BRACE) && !tokens.isAtEnd())
                        if (tokens.match(T_TYPE)) { // local declaration
                                Token<LPCType> typeToken = tokens.previous();

                                locals(typeToken, statements);
                        } else
                                statements.add(statement());

                tokens.consume(T_RIGHT_BRACE, isMethodBody ? "Expect '}' after method body."
                                : "Expect '}' after block.");

                locals.endScope();

                if (isMethodBody && needsImplicitReturn(statements)) {
                        if (currentReturnType == LPCType.LPCVOID)
                                statements.add(implicitReturn());
                        else
                                throw new ParseException("Non-void methods must end with an explicit return statement.",
                                                tokens.previous());
                }

                return new ASTStmtBlock(currLine(), statements);
        }

    private void locals(Token<LPCType> typeToken, List<ASTStatement> statements) {
        do {
            Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect local variable name.");
            Symbol symbol = new Symbol(typeToken, nameToken);

            if (locals.hasCollision(symbol.name()))
                throw new ParseException("Already a local variable named '" + symbol.name() + "' in current scope.", nameToken);

            ASTLocal local = new ASTLocal(currLine(), symbol);

            locals.add(local, true); // sets slot # and depth

            if (tokens.match(T_EQUAL)) { // local assignment
                ASTExprLocalStore expr = new ASTExprLocalStore(currLine(), local, expression());
                ASTStmtExpression exprStmt = new ASTStmtExpression(currLine(), expr);

                statements.add(exprStmt);
            }
        } while (tokens.match(T_COMMA));

        tokens.consume(T_SEMICOLON, "Expect ';' after local variable declaration.");
    }

        public ASTStatement statement() {
                if (tokens.match(T_IF))
                        return ifStatement();
                else if (tokens.match(T_RETURN))
                        return returnStatement();
                else if (tokens.match(T_LEFT_BRACE))
                        return block(false);
                else
                        return expressionStatement();
        }

        private boolean needsImplicitReturn(List<ASTStatement> statements) {
                return statements.isEmpty() || !(statements.get(statements.size() - 1) instanceof ASTStmtReturn);
        }

        private ASTStmtReturn implicitReturn() {
                SourceSpan span = (tokens != null) ? tokens.previous().span() : null;

                switch (currentReturnType) {
                case LPCINT:
                case LPCSTATUS:
                        return new ASTStmtReturn(currLine(), new ASTExprLiteralInteger(currLine(),
                                        new Token<Integer>(TokenType.T_INT_LITERAL, "0", 0, span)));
                case LPCSTRING:
                case LPCOBJECT:
                case LPCMIXED:
                        return new ASTStmtReturn(currLine(), new ASTExprNull(currLine()));
                case LPCVOID:
                        return new ASTStmtReturn(currLine(), null);
                default:
                        throw new ParseException("Unsupported implicit return type: " + currentReturnType, tokens.current());
                }
        }

        private boolean isReturnTypeCompatible(LPCType expected, LPCType actual) {
                if (expected == LPCType.LPCMIXED)
                        return true;

                if (actual == null)
                        return false;

                if ((expected == LPCType.LPCINT && actual == LPCType.LPCSTATUS)
                                || (expected == LPCType.LPCSTATUS && actual == LPCType.LPCINT))
                        return true;

                if (actual == LPCType.LPCNULL)
                        return expected == LPCType.LPCOBJECT || expected == LPCType.LPCSTRING
                                        || expected == LPCType.LPCMIXED;

                return expected == actual;
        }

    private static class FieldDeclarator {
        private final Symbol symbol;
        private final ASTExpression initializer;

        FieldDeclarator(Symbol symbol, ASTExpression initializer) {
            this.symbol = symbol;
            this.initializer = initializer;
        }

        Symbol symbol() {
            return symbol;
        }

        ASTExpression initializer() {
            return initializer;
        }
    }

    private ASTStatement ifStatement() {
        ASTExpression expr = ifCondition();
        ASTStatement stmtThen = statement();

        if (tokens.match(T_ELSE))
            return new ASTStmtIfThenElse(currLine(), expr, stmtThen, statement());
        else
            return new ASTStmtIfThenElse(currLine(), expr, stmtThen, null);
    }

    private ASTExpression ifCondition() {
        tokens.consume(T_LEFT_PAREN, "Expect '(' after if.");

        ASTExpression expr = expression();

        tokens.consume(T_RIGHT_PAREN, "Expect ')' after if condition.");

        return expr;
    }

    private ASTStmtReturn returnStatement() {
        Token<?> returnToken = tokens.previous();

        if (tokens.match(T_SEMICOLON)) {
            if (currentReturnType != LPCType.LPCVOID)
                throw new ParseException(
                        "Non-void methods must return a value of type " + currentReturnType + ".", returnToken);

            return new ASTStmtReturn(currLine(), null);
        }

        ASTExpression expr = expression();

        if (currentReturnType == LPCType.LPCVOID)
            throw new ParseException("Void methods cannot return a value.", tokens.previous());

        LPCType returnType = expr.lpcType();

        if (!isReturnTypeCompatible(currentReturnType, returnType))
            throw new ParseException(
                    "Return type mismatch: expected " + currentReturnType + " but found " + returnType + ".",
                    tokens.previous());

        tokens.consume(T_SEMICOLON, "Expect ';' after return statement.");

        return new ASTStmtReturn(currLine(), expr);
    }

    private ASTStmtExpression expressionStatement() {
        ASTExpression expr = expression();

        tokens.consume(T_SEMICOLON, "Expect ';' after expression.");

        return new ASTStmtExpression(currLine(), expr);
    }

    public ASTExpression expression() {
        return parsePrecedence(PrattParser.Precedence.PREC_ASSIGNMENT);
    }

    public ASTExpression parsePrecedence(int precedence) {
        tokens.advance();

        PrefixParselet pp = PrattParser.getRule(tokens.previous()).prefix();

        if (pp == null)
            throw new ParseException("Expect expression.", tokens.current());

        boolean canAssign = (precedence <= PrattParser.Precedence.PREC_ASSIGNMENT);

        ASTExpression expr = pp.parse(this, canAssign);

        while (precedence <= PrattParser.getRule(tokens.current()).precedence()) {
            tokens.advance();

            InfixParselet ip = PrattParser.getRule(tokens.previous()).infix();

            if (ip == null)
                throw new ParseException("Expect expression.", tokens.current());

            expr = ip.parse(this, expr, canAssign);
        }

        if (canAssign)
            if (tokens.match(T_EQUAL) || tokens.match(T_PLUS_EQUAL) || tokens.match(T_MINUS_EQUAL))
                throw new ParseException("Invalid assignment target.", tokens.current());

        return expr;
    }

    private void skipInitializer() {
        int parenDepth = 0;
        int bracketDepth = 0;

        while (!tokens.isAtEnd()) {
            TokenType type = tokens.current().type();

            if (type == T_LEFT_PAREN) {
                parenDepth++;
                tokens.advance();
            } else if (type == T_RIGHT_PAREN) {
                if (parenDepth == 0 && bracketDepth == 0)
                    break;

                parenDepth--;
                tokens.advance();
            } else if (type == T_LEFT_BRACKET) {
                bracketDepth++;
                tokens.advance();
            } else if (type == T_RIGHT_BRACKET) {
                if (parenDepth == 0 && bracketDepth == 0)
                    break;

                bracketDepth--;
                tokens.advance();
            } else if ((parenDepth == 0) && (bracketDepth == 0) && (type == T_COMMA || type == T_SEMICOLON))
                break;
            else
                tokens.advance();
        }

        if (tokens.isAtEnd())
            throw new ParseException("Unterminated initializer.", tokens.current());
    }
}
