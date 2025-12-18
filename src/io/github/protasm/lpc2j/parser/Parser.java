package io.github.protasm.lpc2j.parser;

import static io.github.protasm.lpc2j.token.TokenType.T_COMMA;
import static io.github.protasm.lpc2j.token.TokenType.T_ELSE;
import static io.github.protasm.lpc2j.token.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.token.TokenType.T_IF;
import static io.github.protasm.lpc2j.token.TokenType.T_INHERIT;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_BRACE;
import static io.github.protasm.lpc2j.token.TokenType.T_LEFT_PAREN;
import static io.github.protasm.lpc2j.token.TokenType.T_MINUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_PLUS_EQUAL;
import static io.github.protasm.lpc2j.token.TokenType.T_RETURN;
import static io.github.protasm.lpc2j.token.TokenType.T_RIGHT_BRACE;
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
import io.github.protasm.lpc2j.token.Token;
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

        this.tokens = tokens;

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
                Declaration declaration = declarationSymbol();
                Symbol symbol = declaration.symbol();

                if (tokens.match(T_LEFT_PAREN))
                        method(symbol, define);
                else if (declaration.inferredUntypedMethod())
                        throw new ParseException("Expect '(' after method name.", tokens.current());
                else
                        field(symbol, define); // TODO: field(s)
        }

        private Declaration declarationSymbol() {
                if (tokens.check(T_TYPE)) {
                        Token<LPCType> typeToken = tokens.consume(T_TYPE, "Expect property type.");
                        Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect property name.");
                        Symbol symbol = new Symbol(typeToken, nameToken);

                        if (options.requireUntyped() && tokens.check(T_LEFT_PAREN))
                                throw new ParseException("Method declarations must omit return types when --require-untyped is set.", typeToken);

                        return new Declaration(symbol, false);
                }

                if (options.requireUntyped() && tokens.check(T_IDENTIFIER) && (tokens.peek(1).type() == T_LEFT_PAREN)) {
                        Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect method name.");
                        Symbol symbol = new Symbol(LPCType.LPCMIXED, nameToken.lexeme());

                        return new Declaration(symbol, true);
                }

                String message = options.requireUntyped() ? "Expect method name." : "Expect property type.";

                throw new ParseException(message, tokens.current());
        }

    private void field(Symbol symbol, boolean define) {
        if (!define) {
            skipFieldInit();

            ASTField field = new ASTField(currLine(), currObj.name(), symbol);

            currObj.fields().put(field.symbol().name(), field);

            return;
        }

        // TODO: what about int x, y = 45, z;?

        if (tokens.match(T_EQUAL)) {
            ASTField field = currObj.fields().get(symbol.name());
            ASTExpression initializer = expression();

            field.setInitializer(initializer);
        }

        tokens.consume(T_SEMICOLON, "Expect ';' after field declaration.");
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

    private void skipFieldInit() {
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

        throw new ParseException("Unmatched '{' in method body.");
    }

        private ASTParameters parameters() {
                ASTParameters params = new ASTParameters(currLine());

                if (tokens.match(T_RIGHT_PAREN)) // No parameters
                        return params;

                do {
                        Symbol symbol;

                        if (options.requireUntyped()) {
                                if (tokens.check(T_TYPE))
                                        throw new ParseException("Method parameters must be untyped when --require-untyped is set.", tokens.current());

                                Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect parameter name.");

                                symbol = new Symbol(LPCType.LPCMIXED, nameToken.lexeme());
                        } else {
                                Token<LPCType> typeToken = tokens.consume(T_TYPE, "Expect parameter type.");
                                Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect parameter name.");

                                symbol = new Symbol(typeToken, nameToken);
                        }

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

        private ASTStmtBlock block(boolean allowImplicitReturn) {
                locals.beginScope();

                List<ASTStatement> statements = new ArrayList<>();

        while (!tokens.check(T_RIGHT_BRACE) && !tokens.isAtEnd())
            if (tokens.match(T_TYPE)) { // local declaration //TODO: declaration(s)
                ASTLocal local = local();

                locals.add(local, true); // sets slot # and depth

                // TODO: what about int x, y = 45, z;?

                if (tokens.match(T_EQUAL)) { // local assignment
                    ASTExprLocalStore expr = new ASTExprLocalStore(currLine(), local, expression());
                    ASTStmtExpression exprStmt = new ASTStmtExpression(currLine(), expr);

                    statements.add(exprStmt);
                }

                tokens.consume(T_SEMICOLON, "Expect ';' after local variable declaration.");
            } else
                statements.add(statement());

                tokens.consume(T_RIGHT_BRACE, "Expect '}' after method body.");

                locals.endScope();

                if (allowImplicitReturn && needsImplicitReturn(statements))
                        statements.add(implicitReturn());

                return new ASTStmtBlock(currLine(), statements);
        }

    private ASTLocal local() {
        Token<LPCType> typeToken = tokens.previous();
        Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect local variable name.");
        Symbol symbol = new Symbol(typeToken, nameToken);

        if (locals.hasCollision(symbol.name()))
            throw new ParseException("Already a local variable named '" + symbol.name() + "' in current scope.");

        return new ASTLocal(currLine(), symbol);
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
                switch (currentReturnType) {
                case LPCINT:
                case LPCSTATUS:
                        return new ASTStmtReturn(currLine(), new ASTExprLiteralInteger(currLine(),
                                        new Token<Integer>(TokenType.T_INT_LITERAL, "0", 0, currLine())));
                case LPCSTRING:
                case LPCOBJECT:
                case LPCMIXED:
                        return new ASTStmtReturn(currLine(), new ASTExprNull(currLine()));
                case LPCVOID:
                        return new ASTStmtReturn(currLine(), null);
                default:
                        throw new ParseException("Unsupported implicit return type: " + currentReturnType);
                }
        }

        private static class Declaration {
                private final Symbol symbol;
                private final boolean inferredUntypedMethod;

                Declaration(Symbol symbol, boolean inferredUntypedMethod) {
                        this.symbol = symbol;
                        this.inferredUntypedMethod = inferredUntypedMethod;
                }

                Symbol symbol() {
                        return symbol;
                }

                boolean inferredUntypedMethod() {
                        return inferredUntypedMethod;
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
        if (tokens.match(T_SEMICOLON))
            return new ASTStmtReturn(currLine(), null);

        ASTExpression expr = expression();

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
}
