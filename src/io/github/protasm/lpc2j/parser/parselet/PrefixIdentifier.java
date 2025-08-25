package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.scanner.TokenType.T_EQUAL;
import static io.github.protasm.lpc2j.scanner.TokenType.T_IDENTIFIER;
import static io.github.protasm.lpc2j.scanner.TokenType.T_LEFT_PAREN;
import static io.github.protasm.lpc2j.scanner.TokenType.T_RIGHT_ARROW;

import io.github.protasm.lpc2j.efun.Efun;
import io.github.protasm.lpc2j.efun.EfunRegistry;
import io.github.protasm.lpc2j.parser.ParseException;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
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

		// Call?
		if (parser.tokens().check(T_LEFT_PAREN)) {
			ASTArguments args = parser.arguments();

			// Method of same object?
			// TODO: handle overloaded methods
			ASTMethod method = parser.currObj().methods().get(identifier);

			if (method != null) // Call
				return new ASTExprCallMethod(line, method, args);

			// Efun?
			Efun efun = EfunRegistry.lookup(identifier);

			if (efun != null) // Call
				return new ASTExprCallEfun(line, efun, args);

			throw new ParseException("Unrecognized method or function '" + identifier + "'.");
		}

		// Local?
		ASTLocal local = parser.locals().get(identifier);

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

		// Field?
		ASTField field = parser.currObj().fields().get(identifier);

		if (field != null)
			// Invoke?
			if (parser.tokens().match(T_RIGHT_ARROW)) {
				System.out.println("TODO: implement field invocation (PrefixIdentifier.java)");
				return null; // TODO
			// Assign?
			} else if (canAssign && parser.tokens().match(T_EQUAL))
				return new ASTExprFieldStore(line, field, parser.expression());
			// Retrieve.
			else
				return new ASTExprFieldAccess(line, field);

		throw new ParseException("Unrecognized local or field '" + identifier + "'.");
	}
}
