package io.github.protasm.lpc2j.parser.parselet;

import static io.github.protasm.lpc2j.InstrType.IT_CONST_STR;
import static io.github.protasm.lpc2j.InstrType.IT_FIELD_LOAD;
import static io.github.protasm.lpc2j.InstrType.IT_FIELD_STORE;
import static io.github.protasm.lpc2j.InstrType.IT_INVOKE;
import static io.github.protasm.lpc2j.InstrType.IT_INVOKE_OTHER;
import static io.github.protasm.lpc2j.InstrType.IT_LOAD_THIS;
import static io.github.protasm.lpc2j.InstrType.IT_LOC_LOAD;
import static io.github.protasm.lpc2j.InstrType.IT_LOC_STORE;

import io.github.protasm.lpc2j.Field;
import io.github.protasm.lpc2j.Method;
import io.github.protasm.lpc2j.parser.Parser;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprVariable;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExpression;
import io.github.protasm.lpc2j.scanner.Token;

public class PrefixIdentifier implements PrefixParselet {
    @Override
    public ASTExpression parse(Parser parser, boolean canAssign) {
	int line = parser.currLine();
	String identifier = parser.tokens().previous().lexeme();
//	int idx = slotForLocal(identifier);
//
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
	
	if (parser.hasField(identifier)) { // field
	    Field field = cb.getField(identifier);

	    if (canAssign && parser.match(TOKEN_EQUAL)) { // assignment
		cb.currMethod().emitInstr(IT_LOAD_THIS);

		expression();

		cb.currMethod().emitInstr(IT_FIELD_STORE, field);
	    } else { // retrieval
		cb.currMethod().emitInstr(IT_LOAD_THIS);
		cb.currMethod().emitInstr(IT_FIELD_LOAD, field);
	    }
	}

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
