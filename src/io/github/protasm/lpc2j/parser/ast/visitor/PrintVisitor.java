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
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
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
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;

public final class PrintVisitor implements ASTVisitor {
    private int indentLvl;

    public PrintVisitor() {
        indentLvl = 0;
    }

    private void doOutput(String str) {
        System.out.println(String.format("%s%s", "  ".repeat(indentLvl), str));
    }

    @Override
    public void visitArgument(ASTArgument argument) {
        doOutput(argument.className());
        indentLvl++;
        argument.expression().accept(this);
        indentLvl--;
    }

    @Override
    public void visitArguments(ASTArguments arguments) {
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
    public void visitExprCallEfun(ASTExprCallEfun expr) {
        doOutput(String.format("%s%s", expr.className(), expr.efun().symbol()));
        indentLvl++;
        expr.arguments().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprCallMethod(ASTExprCallMethod expr) {
        doOutput(String.format("%s%s", expr.className(), expr.method().symbol()));
        indentLvl++;
        expr.arguments().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprFieldAccess(ASTExprFieldAccess expr) {
        doOutput(expr.className());
        indentLvl++;
        expr.field().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprFieldStore(ASTExprFieldStore expr) {
        doOutput(expr.className());
        indentLvl++;
        expr.field().accept(this);
        expr.value().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprInvokeLocal(ASTExprInvokeLocal expr) {
        doOutput(
                String.format("%s([%s] slot=%d, methodName=%s)", expr.className(), expr.lpcType(), expr.slot(),
                        expr.methodName()));
        indentLvl++;
        expr.args().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprLiteralFalse(ASTExprLiteralFalse expr) {
        doOutput(expr.className());
    }

    @Override
    public void visitExprLiteralInteger(ASTExprLiteralInteger expr) {
        doOutput(String.format("%s[%s]", expr.className(), expr.value()));
    }

    @Override
    public void visitExprLiteralString(ASTExprLiteralString expr) {
        doOutput(String.format("%s[\"%s\"],", expr.className(), expr.value()));
    }

    @Override
    public void visitExprLiteralTrue(ASTExprLiteralTrue expr) {
        doOutput(expr.className());
    }

    @Override
    public void visitExprLocalAccess(ASTExprLocalAccess expr) {
        doOutput(expr.className());
        indentLvl++;
        expr.local().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprLocalStore(ASTExprLocalStore expr) {
        doOutput(expr.className());
        indentLvl++;
        expr.local().accept(this);
        expr.value().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprNull(ASTExprNull expr) {
        doOutput(expr.className());
    }

    @Override
    public void visitExprOpBinary(ASTExprOpBinary expr) {
        doOutput(String.format("%s[%s]", expr.className(), expr.operator()));
        indentLvl++;
        expr.left().accept(this);
        expr.right().accept(this);
        indentLvl--;
    }

    @Override
    public void visitExprOpUnary(ASTExprOpUnary expr) {
        doOutput(String.format("%s[%s]", expr.className(), expr.operator()));
        indentLvl++;
        expr.right().accept(this);
        indentLvl--;
    }

    @Override
    public void visitField(ASTField field) {
        doOutput(String.format("%s%s", field.className(), field.symbol()));
        indentLvl++;
        if (field.initializer() != null)
            field.initializer().accept(this);
        indentLvl--;
    }

    @Override
    public void visitFields(ASTFields fields) {
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

    @Override
    public void visitLocal(ASTLocal local) {
        doOutput(String.format("%s[%s, slot=%d, depth=%d]", local.className(), local.symbol(), local.slot(),
                local.scopeDepth()));
    }

    @Override
    public void visitMethod(ASTMethod method) {
        doOutput(String.format("%s%s", method.className(), method.symbol()));
        indentLvl++;
        if (method.parameters() != null)
            method.parameters().accept(this);
        method.body().accept(this);
        indentLvl--;
    }

    @Override
    public void visitMethods(ASTMethods methods) {
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
    public void visitObject(ASTObject object) {
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

    @Override
    public void visitParameter(ASTParameter parameter) {
        doOutput(String.format("%s%s", parameter.className(), parameter.symbol()));
    }

    @Override
    public void visitParameters(ASTParameters parameters) {
        if (parameters.size() == 0)
            doOutput("[No Parameters]");
        else
            for (ASTParameter param : parameters)
                param.accept(this);

        System.out.println();
    }

    @Override
    public void visitStmtBlock(ASTStmtBlock stmt) {
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

    @Override
    public void visitStmtExpression(ASTStmtExpression stmt) {
        doOutput(stmt.className());
        indentLvl++;
        stmt.expression().accept(this);
        indentLvl--;
    }

    @Override
    public void visitStmtIfThenElse(ASTStmtIfThenElse stmt) {
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

    @Override
    public void visitStmtReturn(ASTStmtReturn stmt) {
        doOutput(stmt.className());
        indentLvl++;
        if (stmt.returnValue() != null)
            stmt.returnValue().accept(this);
        else
            doOutput("[No Return Value]");
        indentLvl--;
    }
}
