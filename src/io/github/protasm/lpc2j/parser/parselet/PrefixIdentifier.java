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

	Local local = parser.locals().get(identifier); // local?

	if (local != null)
	    if (parser.tokens().match(T_RIGHT_ARROW)) {
		Token<String> nameToken = parser.tokens().consume(T_IDENTIFIER, "Expect method name.");

		return new ASTExprLocalMethodInvoke(line, local.slot(), nameToken.lexeme(), parser.arguments()); // invoke
	    } else if (canAssign && parser.tokens().match(T_EQUAL))
		return new ASTExprLocalStore(line, local, parser.expression()); // set
	    else
		return new ASTExprLocalAccess(line, local); // get

	ASTField field = parser.currObj().fields().get(identifier); // field?

	if (field != null)
	    if (canAssign && parser.tokens().match(T_EQUAL))
		return new ASTExprFieldStore(line, field, parser.expression()); // set
	    else
		return new ASTExprFieldAccess(line, field); // get

	ASTMethod method = parser.currObj().methods().get(identifier); // method of same object?

	if (method != null)
	    return new ASTExprMethodCall(line, method, parser.arguments());

	throw new ParseException("Unrecognized identifier '" + identifier + "'.");

//	if (idx != -1) { // initialized local
//	    if (canAssign && parser.match(TOKEN_EQUAL)) { // assignment
//		cb.currMethod().emitInstr(IT_LOAD_THIS);
//		expression();
//		cb.currMethod().emitInstr(IT_LOC_STORE, idx);
//	    } else if (parser.match(TOKEN_INVOKE)) { // method of another object
//		Token nameToken = parser.parseVariable("Expect method name.");
//		String methodName = nameToken.lexeme();
//
//		cb.currMethod().emitInstr(IT_LOC_LOAD, idx);
//
//		cb.currMethod().emitInstr(IT_CONST_STR, methodName);
//
//		arguments(true);
//
//		cb.currMethod().emitInstr(IT_INVOKE_OTHER);
//	    } else // retrieval
//		cb.currMethod().emitInstr(IT_LOC_LOAD, idx);
//	}

//	if (parser.hasField(identifier)) { // field
//	    Field field = cb.getField(identifier);
//
//	    if (canAssign && parser.match(TOKEN_EQUAL)) { // assignment
//		cb.currMethod().emitInstr(IT_LOAD_THIS);
//
//		expression();
//
//		cb.currMethod().emitInstr(IT_FIELD_STORE, field);
//	    } else { // retrieval
//		cb.currMethod().emitInstr(IT_LOAD_THIS);
//		cb.currMethod().emitInstr(IT_FIELD_LOAD, field);
//	    }
//	}

//	if (cb.hasMethod(identifier)) { // method of same object
//	    Method method = cb.getMethod(identifier);
//
//	    cb.currMethod().emitInstr(IT_LOAD_THIS);
//
//	    arguments(false);
//
//	    cb.currMethod().emitInstr(IT_INVOKE, method.identifier(), method.descriptor());
//	}
//	else if (resolveSuperMethod(name)) //superClass method
//	namedSuperMethod(name);
//	else
//	    parser.error("Unrecognized identifier '" + identifier + "'.");
    }
}
