package io.github.protasm.lpc2j.parser.ast.visitor;

import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTFields;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTMethods;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralTrue;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprNull;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;

public class PrintVisitor {
	private int indentLvl;

	public PrintVisitor() {
		indentLvl = 0;
	}

	private void doOutput(String str) {
		System.out.println(String.format("%s%s", "  ".repeat(indentLvl), str));
	}

	public void visit(ASTArgument argument) {
		doOutput(argument.className());

		indentLvl++;

		argument.expression().accept(this);

		indentLvl--;
	}

	public void visit(ASTArguments arguments) {
		if (arguments.size() == 0) {
			doOutput("[No Arguments]");

			return;
		}

		doOutput("[ARGUMENTS]");

		indentLvl++;

		for (ASTArgument argument : arguments)
			argument.accept(this);

		indentLvl--;
	}

	public void visit(ASTExprCallEfun expr) {
		doOutput(String.format("%s%s", expr.className(), expr.efun().symbol()));

		indentLvl++;

		expr.arguments().accept(this);

		indentLvl--;
	}

	public void visit(ASTExprCallMethod expr) {
		doOutput(String.format("%s%s", expr.className(), expr.method().symbol()));

		indentLvl++;

		expr.arguments().accept(this);

		indentLvl--;
	}

	public void visit(ASTExprFieldAccess expr) {
		doOutput(expr.className());

		indentLvl++;

		expr.field().accept(this);

		indentLvl--;
	}

	public void visit(ASTExprFieldStore expr) {
		doOutput(expr.className());

		indentLvl++;

		expr.field().accept(this);
		expr.value().accept(this);

		indentLvl--;
	}

	public void visit(ASTExprInvokeLocal expr) {
		doOutput(String.format("%s([%s] slot=%d, methodName=%s)", expr.className(), expr.lpcType(), expr.slot(),
				expr.methodName()));

		indentLvl++;

		expr.args().accept(this);

		indentLvl--;
	}

	public void visit(ASTExprLiteralFalse expr) {
		doOutput(expr.className());
	}

	public void visit(ASTExprLiteralInteger expr) {
		doOutput(String.format("%s[%s]", expr.className(), expr.value()));
	}

	public void visit(ASTExprLiteralString expr) {
		doOutput(String.format("%s[\"%s\"],", expr.className(), expr.value()));
	}

	public void visit(ASTExprLiteralTrue expr) {
		doOutput(expr.className());
	}

	public void visit(ASTExprLocalAccess expr) {
		doOutput(expr.className());

		indentLvl++;

		expr.local().accept(this);

		indentLvl--;
	}

	public void visit(ASTExprLocalStore expr) {
		doOutput(expr.className());

		indentLvl++;

		expr.local().accept(this);
		expr.value().accept(this);

		indentLvl--;
	}

	public void visit(ASTExprNull expr) {
		doOutput(expr.className());
	}

	public void visit(ASTExprOpBinary expr) {
		doOutput(String.format("%s[%s]", expr.className(), expr.operator()));

		indentLvl++;

		expr.left().accept(this);
		expr.right().accept(this);

		indentLvl--;
	}

	public void visit(ASTExprOpUnary expr) {
		doOutput(String.format("%s[%s]", expr.className(), expr.operator()));

		indentLvl++;

		expr.right().accept(this);

		indentLvl--;
	}

	public void visit(ASTField field) {
		doOutput(String.format("%s%s", field.className(), field.symbol()));

		indentLvl++;

		if (field.initializer() != null)
			field.initializer().accept(this);

		indentLvl--;
	}

	public void visit(ASTFields fields) {
		if (fields.size() == 0) {
			doOutput("[No Fields]");

			System.out.println();

			return;
		}

		doOutput("[FIELDS]");

		indentLvl++;

		for (ASTField field : fields) {
			field.accept(this);

			System.out.println();
		}

		indentLvl--;
	}

	public void visit(ASTLocal local) {
		doOutput(String.format("%s[%s, slot=%d, depth=%d]", local.className(), local.symbol(), local.slot(),
				local.scopeDepth()));
	}

	public void visit(ASTMethod method) {
		doOutput(String.format("%s%s", method.className(), method.symbol()));

		indentLvl++;

		method.parameters().accept(this);
		method.body().accept(this);

		indentLvl--;
	}

	public void visit(ASTMethods methods) {
		if (methods.size() == 0) {
			doOutput("[No Methods]");

			return;
		}

		doOutput("[METHODS]");

		indentLvl++;

		int count = 0;

		for (ASTMethod method : methods) {
			method.accept(this);

			if (++count < methods.size())
				System.out.println();
		}

		indentLvl--;
	}

	public void visit(ASTObject object) {
		if (object.parentName() != null)
			doOutput(String.format("%s(%s inherits %s)", object.className(), object.name(), object.parentName()));
		else
			doOutput(String.format("%s(%s)", object.className(), object.name()));

		indentLvl++;

		object.fields().accept(this);
		object.methods().accept(this);

		indentLvl--;

		doOutput("End Object");
	}

	public void visit(ASTParameter parameter) {
		doOutput(String.format("%s%s", parameter.className(), parameter.symbol()));
	}

	public void visit(ASTParameters parameters) {
		if (parameters.size() == 0)
			doOutput("[No Parameters]");
		else
			for (ASTParameter param : parameters)
				param.accept(this);

		System.out.println();
	}

	public void visit(ASTStmtBlock stmt) {
		doOutput(String.format("%s[%d stmt(s)]", stmt.className(), stmt.size()));

		indentLvl++;

		int count = 0;

		for (ASTStatement statement : stmt) {
			statement.accept(this);

			if (++count < stmt.size())
				System.out.println();
		}

		indentLvl--;
	}

	public void visit(ASTStmtExpression stmt) {
		doOutput(stmt.className());

		indentLvl++;

		stmt.expression().accept(this);

		indentLvl--;
	}

	public void visit(ASTStmtIfThenElse stmt) {
		doOutput(stmt.className());

		indentLvl++;

		doOutput("[IF]");

		stmt.condition().accept(this);

		System.out.println();

		doOutput("[THEN]");

		stmt.thenBranch().accept(this);

		System.out.println();

		if (stmt.elseBranch() != null) {
			doOutput("[ELSE]");

			stmt.elseBranch().accept(this);
		} else
			doOutput("[No Else Condition]");

		indentLvl--;
	}

	public void visit(ASTStmtReturn stmt) {
		doOutput(stmt.className());

		indentLvl++;

		if (stmt.returnValue() != null)
			stmt.returnValue().accept(this);
		else
			doOutput("[No Return Value]");

		indentLvl--;
	}
}
