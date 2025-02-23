package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.scanner.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.scanner.TokenType.T_RIGHT_ARROW;

import java.lang.reflect.Method;

import io.github.protasm.lpc2j.compiler.GfunsIntfc;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCall;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallGfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.Token;

public class PrefixIdentifier implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
	int line = parser.currLine();
	String identifier = parser.tokens().previous().lexeme();

	ASTLocal local = parser.locals().get(identifier);

	// Local?
	if (local != null)
	    // Invoke?
	    if (parser.tokens().match(T_RIGHT_ARROW)) {
		Token<String> nameToken = parser.tokens().consume(T_IDENTIFIER, "Expect method name.");

		return new ASTExprInvokeLocal(line, local.slot(), nameToken.lexeme(), parser.arguments());
		// Assign?
	    } else if (canAssign && parser.tokens().match(T_EQUAL))
		return new ASTExprLocalStore(line, local, parser.expression());
	    // Retrieve.
	    else
		return new ASTExprLocalAccess(line, local);

	ASTField field = parser.currObj().fields().get(identifier);

	// Field?
	if (field != null)
	    // Invoke?
	    if (parser.tokens().match(T_RIGHT_ARROW))
		return null; // TODO
	    // Assign?
	    else if (canAssign && parser.tokens().match(T_EQUAL))
		return new ASTExprFieldStore(line, field, parser.expression());
	    // Retrieve.
	    else
		return new ASTExprFieldAccess(line, field);

	// TODO: handle overloaded methods
	ASTMethod method = parser.currObj().methods().get(identifier);

	// Method of same object?
	if (method != null) {
	    ASTArguments args = parser.arguments();

	    // Call.
	    return new ASTExprCall(line, method, args);
	}

	GfunsIntfc gfuns = parser.gfuns();

	if (gfuns != null) {
	    Method gfun = gfuns.getMethod(identifier);

	    // Global function?
	    if (gfun != null) {
		// TODO: handle overloaded gfuns
		ASTArguments args = parser.arguments();

		// Call.
		return new ASTExprCallGfun(line, gfun, args);
	    }
	}

	throw new ParseException("Unrecognized identifier '" + identifier + "'.");
    }
}
