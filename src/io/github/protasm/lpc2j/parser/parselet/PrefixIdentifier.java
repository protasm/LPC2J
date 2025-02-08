package io.github.protasm.lpc2j.parser.parselet;

import io.github.protasm.lpc2j.parser.Local;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalMethodInvoke;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.Token;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprMethodCall;

import static io.github.protasm.lpc2j.scanner.TokenType.*;

public class PrefixIdentifier implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
	int line = parser.currLine();
	String identifier = parser.tokens().previous().lexeme();

	Local local = parser.locals().get(identifier);

	// Local?
	if (local != null)
	    // Invoke?
	    if (parser.tokens().match(T_RIGHT_ARROW)) {
		Token<String> nameToken = parser.tokens().consume(T_IDENTIFIER, "Expect method name.");

		return new ASTExprLocalMethodInvoke(line, local.slot(), nameToken.lexeme(), parser.arguments());
		// Assign?
	    } else if (canAssign && parser.tokens().match(T_EQUAL))
		return new ASTExprLocalStore(line, local, parser.expression());
	    // Retrieve.
	    else
		return new ASTExprLocalAccess(line, local);

	ASTField field = parser.currObj().fields().get(identifier);

	// Field?
	if (field != null)
	    // Assign?
	    if (canAssign && parser.tokens().match(T_EQUAL))
		return new ASTExprFieldStore(line, field, parser.expression());
	    // Retrieve.
	    else
		return new ASTExprFieldAccess(line, field);

	ASTMethod method = parser.currObj().methods().get(identifier);

	// Method of same object?
	if (method != null)
	    // Call.
	    return new ASTExprMethodCall(line, method, parser.arguments());

	throw new ParseException("Unrecognized identifier '" + identifier + "'.");
    }
}
