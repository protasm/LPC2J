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
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCall;
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

public class PrintVisitor implements PrintVisitorIntfc {
	public static int indentLvl;

	public PrintVisitor() {
		PrintVisitor.indentLvl = 0;
	}

	@Override
	public void visit(ASTObject object) {
		if (object.parentName() != null)
			doOutput(
					String.format(
							"%s(%s inherits %s)",
							object.className(),
							object.name(),
							object.parentName()));
		else
			doOutput(
					String.format(
							"%s(%s)",
							object.className(),
							object.name()));

		indentLvl++;

		object.fields().accept(this);
		object.methods().accept(this);

		indentLvl--;

		doOutput("End Object");
	}

	@Override
	public void visit(ASTFields fields) {
		if (fields.size() == 0) {
			doOutput("[No Fields]");

			return;
		}

		doOutput("[FIELDS]");

		indentLvl++;

		int count = 0;

		for (ASTField field : fields) {
			field.accept(this);

			if (++count < fields.size())
				System.out.println();
		}

		indentLvl--;
	}

	@Override
	public void visit(ASTField field) {
		doOutput(
				String.format(
						"%s%s",
						field.className(),
						field.symbol()));

		indentLvl++;

		if (field.initializer() != null)
			field.initializer().accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTMethods methods) {
		System.out.println();

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

	@Override
	public void visit(ASTMethod method) {
		doOutput(
				String.format(
						"%s%s",
						method.className(),
						method.symbol()));

		indentLvl++;

		method.parameters().accept(this);
		method.body().accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTParameters parameters) {
		if (parameters.size() == 0) {
			doOutput("[No Parameters]");

			return;
		}

		for (ASTParameter param : parameters)
			param.accept(this);
	}

	@Override
	public void visit(ASTParameter parameter) {
		doOutput(
				String.format(
						"%s%s",
						parameter.className(),
						parameter.symbol()));
	}

	@Override
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

	@Override
	public void visit(ASTArgument argument) {
		doOutput(argument.className());

		indentLvl++;

		argument.expression().accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTLocal local) {
		doOutput(
				String.format(
						"%s[%s, slot=%d, depth=%d]",
						local.className(),
						local.symbol(),
						local.slot(),
						local.scopeDepth()));
	}

	@Override
	public void visit(ASTExprCall expr) {
		doOutput(expr.className());

		indentLvl++;

		expr.method().accept(this);
		expr.arguments().accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTExprFieldAccess expr) {
		doOutput(expr.className());

		indentLvl++;

		expr.field().accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTExprFieldStore expr) {
		doOutput(expr.className());

		indentLvl++;

		expr.field().accept(this);
		expr.value().accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTExprInvokeLocal expr) {
		doOutput(expr.lpcType().toString());

		doOutput(
				String.format(
						"%s(slot=%d, methodName=%s)",
						expr.className(),
						expr.slot(),
						expr.methodName()));

		indentLvl++;

		expr.args().accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTExprLiteralFalse expr) {
		doOutput(expr.className());
	}

	@Override
	public void visit(ASTExprLiteralInteger expr) {
		doOutput(
				String.format(
						"%s[%s]",
						expr.className(),
						expr.value()));
	}

	@Override
	public void visit(ASTExprLiteralString expr) {
		doOutput(
				String.format(
						"%s[\"%s\"],",
						expr.className(),
						expr.value()));
	}

	@Override
	public void visit(ASTExprLiteralTrue expr) {
		doOutput(
				String.format(
						"%s[\"%s\"],",
						expr.className()));
	}

	@Override
	public void visit(ASTExprLocalAccess expr) {
		doOutput(expr.className());

		indentLvl++;

		expr.local().accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTExprLocalStore expr) {
//		doOutput(expr.className());
//		
//		indentLvl++;
//		
//		expr.local().accept(this);
//		expr.value().accept(this);
//		
//		indentLvl--;
	}

	@Override
	public void visit(ASTExprNull expr) {
		doOutput("TODO null");
	}

	@Override
	public void visit(ASTExprOpBinary expr) {
		doOutput("TODO ASTExprOpBinary");
	}

	@Override
	public void visit(ASTExprOpUnary expr) {
		doOutput("TODO op unary");
	}

	@Override
	public void visit(ASTStmtBlock stmt) {
		System.out.println();

		doOutput(stmt.className());

		indentLvl++;

		if (stmt.statements().size() == 0) {
			doOutput("[No Statements]");

			return;
		}

		for (ASTStatement statement : stmt.statements())
			statement.accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTStmtExpression stmt) {
		doOutput(stmt.className());

		indentLvl++;

		stmt.expression().accept(this);

		indentLvl--;
	}

	@Override
	public void visit(ASTStmtIfThenElse stmt) {
		doOutput(stmt.className());

		doOutput("[IF]");

		stmt.condition().accept(this);

		doOutput("[THEN]");

		stmt.thenBranch().accept(this);

		if (stmt.elseBranch() != null) {
			doOutput("[ELSE]");

			stmt.elseBranch().accept(this);
		} else
			doOutput("[No Else Condition]");
	}

	@Override
	public void visit(ASTStmtReturn stmt) {
		doOutput(stmt.className());

		indentLvl++;

		if (stmt.value() != null)
			stmt.value().accept(this);
		else
			doOutput("[No Return Value]");

		indentLvl--;
	}

	private static void doOutput(String str) {
		System.out.println(
				String.format(
						"%s%s",
						"  ".repeat(indentLvl),
						str));
	}
}
