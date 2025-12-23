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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTInherit;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.parselet.InfixParselet;
import io.github.protasm.lpc2j.parser.parselet.PrefixParselet;
import io.github.protasm.lpc2j.preproc.Preprocessor;
import io.github.protasm.lpc2j.runtime.RuntimeContext;
import io.github.protasm.lpc2j.token.Token;
import io.github.protasm.lpc2j.token.TokenClassifier;
import io.github.protasm.lpc2j.token.TokenType;
import io.github.protasm.lpc2j.token.TokenList;

public class Parser {
        private TokenList tokens;
        private ASTObject currObj;
        private Locals locals;
        private ASTMethod currentMethod;
        private final ParserOptions options;
        private final RuntimeContext runtimeContext;
        private final Map<String, Integer> fieldDefinitionIndex = new HashMap<>();
        private final Map<String, Integer> methodDefinitionIndex = new HashMap<>();

        public Parser() {
                this(new RuntimeContext(Preprocessor.rejectingResolver()), ParserOptions.defaults());
        }

        public Parser(ParserOptions options) {
                this(new RuntimeContext(Preprocessor.rejectingResolver()), options);
        }

        public Parser(RuntimeContext runtimeContext) {
                this(runtimeContext, ParserOptions.defaults());
        }

        public Parser(RuntimeContext runtimeContext, ParserOptions options) {
                this.runtimeContext = (runtimeContext != null) ? runtimeContext : new RuntimeContext(Preprocessor.rejectingResolver());
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

    public RuntimeContext runtimeContext() {
        return runtimeContext;
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
        while (!tokens.isAtEnd()) {
            if (tokens.match(T_INHERIT)) {
                Token<String> parentToken = consumeInheritPath();
                currObj.addInherit(new ASTInherit(parentToken.line(), parentToken.lexeme()));
                continue;
            }

            property(false);
        }
    }

    private void definitions() {
        tokens.reset();
        fieldDefinitionIndex.clear();
        methodDefinitionIndex.clear();

        while (!tokens.isAtEnd()) {
            if (tokens.match(T_INHERIT)) {
                Token<String> parentToken = consumeInheritPath();

                if (currObj.parentName() == null)
                    currObj.setParentName(parentToken.lexeme());

                continue;
            }

            property(true);
        }
    }

    private Token<String> consumeInheritPath() {
        Token<String> parentToken = tokens.consume(T_STRING_LITERAL, "Expect string after 'inherit'.");

        tokens.consume(T_SEMICOLON, "Expect ';' after inherited object path.");

        return parentToken;
    }

    private void property(boolean define) {
        Symbol symbol = declarationSymbol();
        int declarationLine = tokens.previous().line();
        boolean hasType = symbol.declaredTypeName() != null;

        if (tokens.match(T_LEFT_PAREN))
            method(symbol, define, declarationLine);
        else if (hasType)
            field(symbol, define, declarationLine);
        else
            throw new ParseException("Untyped declarations must be functions.", tokens.current());
    }

    private Symbol declarationSymbol() {
        if (!tokens.check(T_IDENTIFIER))
            throw new ParseException("Expect property type or name.", tokens.current());

        Token<String> firstToken = tokens.consume(T_IDENTIFIER, "Expect property type or name.");
        boolean isArrayType = tokens.match(TokenType.T_STAR);

        if (tokens.check(T_IDENTIFIER)) {
            Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect property name.");
            String declaredTypeName = firstToken.lexeme() + (isArrayType ? "*" : "");
            return new Symbol(declaredTypeName, nameToken.lexeme());
        }

        return new Symbol((String) null, firstToken.lexeme());
    }

    private void field(Symbol symbol, boolean define, int declarationLine) {
        if (define && locals == null)
            locals = new Locals();

        List<FieldDeclarator> declarators = fieldDeclarators(symbol, define);

        if (!define) {
            for (FieldDeclarator declarator : declarators) {
                ASTField field = new ASTField(declarationLine, currObj.name(), declarator.symbol());

                currObj.fields().put(field.symbol().name(), field);
            }

            return;
        }

        for (FieldDeclarator declarator : declarators) {
            int definitionIndex = nextFieldDefinitionIndex(declarator.symbol().name());
            ASTField field = currObj.fields().get(declarator.symbol().name(), definitionIndex);

            if (field == null) {
                field = new ASTField(declarationLine, currObj.name(), declarator.symbol(), false);
                currObj.fields().put(field.symbol().name(), field);
            }

            field.markDefined();
            field.setInitializer(declarator.initializer());
        }
    }

    private int nextFieldDefinitionIndex(String name) {
        return fieldDefinitionIndex.merge(name, 1, Integer::sum) - 1;
    }

    private List<FieldDeclarator> fieldDeclarators(Symbol symbol, boolean define) {
        List<FieldDeclarator> declarators = new ArrayList<>();

        declarators.add(fieldDeclarator(symbol, define));

        while (tokens.match(T_COMMA)) {
            Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect field name.");
            Symbol additionalSymbol = new Symbol(symbol.declaredTypeName(), nameToken.lexeme());

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

        private void method(Symbol symbol, boolean define, int declarationLine) {
                if (!define) {
                        skipMethodBody();

                        ASTMethod method = new ASTMethod(declarationLine, currObj.name(), symbol);

            currObj.methods().put(method.symbol().name(), method);

            return;
        }

                int definitionIndex = nextMethodDefinitionIndex(symbol.name());
                ASTMethod method = currObj.methods().get(symbol.name(), definitionIndex);

                if (method == null) {
                        method = new ASTMethod(declarationLine, currObj.name(), symbol, false);
                        currObj.methods().put(method.symbol().name(), method);
                }

                locals = new Locals();
                currentMethod = method;

                method.markDefined();
                method.setParameters(parameters());

                tokens.consume(T_LEFT_BRACE, "Expect '{' after method declaration.");

                method.setBody(block(true));

                currentMethod = null;
        }

    private int nextMethodDefinitionIndex(String name) {
        return methodDefinitionIndex.merge(name, 1, Integer::sum) - 1;
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
            Token<String> firstToken = tokens.consume(T_IDENTIFIER, "Expect parameter name or type.");
            Token<String> nameToken = null;
            String declaredType = null;

            boolean isArrayType = tokens.match(TokenType.T_STAR);

            if (tokens.check(T_IDENTIFIER)) {
                nameToken = tokens.consume(T_IDENTIFIER, "Expect parameter name.");
                declaredType = firstToken.lexeme() + (isArrayType ? "*" : "");
            } else {
                nameToken = firstToken;
            }

            Symbol symbol = new Symbol(declaredType, nameToken.lexeme());

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
                        if (startsLocalDeclaration()) { // local declaration
                                Token<String> typeToken = tokens.consume(T_IDENTIFIER, "Expect local type.");

                                locals(typeToken, statements);
                        } else
                                statements.add(statement());

                tokens.consume(T_RIGHT_BRACE, isMethodBody ? "Expect '}' after method body."
                                : "Expect '}' after block.");

                locals.endScope();

                return new ASTStmtBlock(currLine(), statements);
        }

    private void locals(Token<String> typeToken, List<ASTStatement> statements) {
        do {
            boolean isArrayType = tokens.match(TokenType.T_STAR);
            Token<String> nameToken = tokens.consume(T_IDENTIFIER, "Expect local variable name.");
            String declaredType = typeToken.lexeme() + (isArrayType ? "*" : "");
            Symbol symbol = new Symbol(declaredType, nameToken.lexeme());

            if (locals.hasCollision(symbol.name()))
                throw new ParseException("Already a local variable named '" + symbol.name() + "' in current scope.", nameToken);

            ASTLocal local = new ASTLocal(currLine(), symbol);

            locals.add(local, true); // sets slot # and depth
            if (currentMethod != null)
                currentMethod.addLocal(local);

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
        if (tokens.match(T_SEMICOLON)) {
            return new ASTStmtReturn(currLine(), null);
        }

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

    private boolean startsLocalDeclaration() {
        if (!tokens.check(T_IDENTIFIER))
            return false;

        Token<?> next = tokens.peek(1);

        if (next.type() == T_IDENTIFIER)
            return true;

        if (next.type() == TokenType.T_STAR) {
            Token<?> after = tokens.peek(2);
            return after.type() == T_IDENTIFIER;
        }

        return false;
    }
}
