package io.github.protasm.lpc2j.parser.ast.visitor;

import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTFields;
import io.github.protasm.lpc2j.parser.ast.ASTInherit;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTMethods;
import io.github.protasm.lpc2j.parser.ast.ASTNode;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierCall;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprIdentifierStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeIdentifier;
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
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;

/**
 * Unified visitor entry point for walking AST nodes. Implementors override the granular {@code
 * visit*} methods they care about; the default {@link #visit(ASTNode)} dispatches to the correct
 * specialization using pattern matching.
 */
public interface ASTVisitor {
    default void visit(ASTNode node) {
        if (node == null)
            return;

        switch (node) {
        case ASTArgument argument -> visitArgument(argument);
        case ASTArguments arguments -> visitArguments(arguments);
        case ASTField field -> visitField(field);
        case ASTFields fields -> visitFields(fields);
        case ASTInherit inherit -> visitInherit(inherit);
        case ASTLocal local -> visitLocal(local);
        case ASTMethod method -> visitMethod(method);
        case ASTMethods methods -> visitMethods(methods);
        case ASTObject object -> visitObject(object);
        case ASTParameter parameter -> visitParameter(parameter);
        case ASTParameters parameters -> visitParameters(parameters);
        case ASTExprCallEfun exprCallEfun -> visitExprCallEfun(exprCallEfun);
        case ASTExprCallMethod exprCallMethod -> visitExprCallMethod(exprCallMethod);
        case ASTExprFieldAccess exprFieldAccess -> visitExprFieldAccess(exprFieldAccess);
        case ASTExprFieldStore exprFieldStore -> visitExprFieldStore(exprFieldStore);
        case ASTExprIdentifierAccess exprIdentifierAccess -> visitExprIdentifierAccess(exprIdentifierAccess);
        case ASTExprIdentifierCall exprIdentifierCall -> visitExprIdentifierCall(exprIdentifierCall);
        case ASTExprIdentifierStore exprIdentifierStore -> visitExprIdentifierStore(exprIdentifierStore);
        case ASTExprInvokeIdentifier exprInvokeIdentifier -> visitExprInvokeIdentifier(exprInvokeIdentifier);
        case ASTExprInvokeLocal exprInvokeLocal -> visitExprInvokeLocal(exprInvokeLocal);
        case ASTExprLiteralFalse exprLiteralFalse -> visitExprLiteralFalse(exprLiteralFalse);
        case ASTExprLiteralInteger exprLiteralInteger -> visitExprLiteralInteger(exprLiteralInteger);
        case ASTExprLiteralString exprLiteralString -> visitExprLiteralString(exprLiteralString);
        case ASTExprLiteralTrue exprLiteralTrue -> visitExprLiteralTrue(exprLiteralTrue);
        case ASTExprLocalAccess exprLocalAccess -> visitExprLocalAccess(exprLocalAccess);
        case ASTExprLocalStore exprLocalStore -> visitExprLocalStore(exprLocalStore);
        case ASTExprNull exprNull -> visitExprNull(exprNull);
        case ASTExprOpBinary exprOpBinary -> visitExprOpBinary(exprOpBinary);
        case ASTExprOpUnary exprOpUnary -> visitExprOpUnary(exprOpUnary);
        case ASTExpression expression -> visitExpression(expression);
        case ASTStmtBlock stmtBlock -> visitStmtBlock(stmtBlock);
        case ASTStmtExpression stmtExpression -> visitStmtExpression(stmtExpression);
        case ASTStmtIfThenElse stmtIfThenElse -> visitStmtIfThenElse(stmtIfThenElse);
        case ASTStmtReturn stmtReturn -> visitStmtReturn(stmtReturn);
        case ASTStatement statement -> visitStatement(statement);
        default -> throw new IllegalStateException("Unhandled AST node: " + node.getClass().getName());
        }
    }

    default void visitArgument(ASTArgument argument) {}

    default void visitArguments(ASTArguments arguments) {
        arguments.forEach(this::visit);
    }

    default void visitField(ASTField field) {}

    default void visitFields(ASTFields fields) {
        fields.forEach(this::visit);
    }

    default void visitInherit(ASTInherit inherit) {}

    default void visitLocal(ASTLocal local) {}

    default void visitMethod(ASTMethod method) {}

    default void visitMethods(ASTMethods methods) {
        methods.forEach(this::visit);
    }

    default void visitObject(ASTObject object) {}

    default void visitParameter(ASTParameter parameter) {}

    default void visitParameters(ASTParameters parameters) {
        parameters.forEach(this::visit);
    }

    default void visitExpression(ASTExpression expression) {}

    default void visitStatement(ASTStatement statement) {}

    default void visitExprCallEfun(ASTExprCallEfun expr) {}

    default void visitExprCallMethod(ASTExprCallMethod expr) {}

    default void visitExprFieldAccess(ASTExprFieldAccess expr) {}

    default void visitExprFieldStore(ASTExprFieldStore expr) {}

    default void visitExprIdentifierAccess(ASTExprIdentifierAccess expr) {}

    default void visitExprIdentifierCall(ASTExprIdentifierCall expr) {}

    default void visitExprIdentifierStore(ASTExprIdentifierStore expr) {}

    default void visitExprInvokeLocal(ASTExprInvokeLocal expr) {}

    default void visitExprInvokeIdentifier(ASTExprInvokeIdentifier expr) {}

    default void visitExprLiteralFalse(ASTExprLiteralFalse expr) {}

    default void visitExprLiteralInteger(ASTExprLiteralInteger expr) {}

    default void visitExprLiteralString(ASTExprLiteralString expr) {}

    default void visitExprLiteralTrue(ASTExprLiteralTrue expr) {}

    default void visitExprLocalAccess(ASTExprLocalAccess expr) {}

    default void visitExprLocalStore(ASTExprLocalStore expr) {}

    default void visitExprNull(ASTExprNull expr) {}

    default void visitExprOpBinary(ASTExprOpBinary expr) {}

    default void visitExprOpUnary(ASTExprOpUnary expr) {}

    default void visitStmtBlock(ASTStmtBlock stmt) {}

    default void visitStmtExpression(ASTStmtExpression stmt) {}

    default void visitStmtIfThenElse(ASTStmtIfThenElse stmt) {}

    default void visitStmtReturn(ASTStmtReturn stmt) {}
}
