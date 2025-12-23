package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.Symbol;
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
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedAssignment;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedCall;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedIdentifier;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprUnresolvedInvoke;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;
import io.github.protasm.lpc2j.semantic.SemanticModel;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class SemanticModelPrinter {
    private final PrintStream out;

    SemanticModelPrinter(PrintStream out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    void print(SemanticModel semanticModel) {
        if (semanticModel == null) {
            out.println("SemanticModel: (none)");
            return;
        }

        PrettyNode root = new PrettyNode("SemanticModel");
        root.add(buildObjectNode(semanticModel.astObject()));
        printNode(root, "", true, true);
    }

    private PrettyNode buildObjectNode(ASTObject object) {
        String label =
                (object.parentName() != null)
                        ? "Object " + object.name() + " (inherits " + object.parentName() + ")"
                        : "Object " + object.name();
        PrettyNode objectNode = new PrettyNode(label);

        List<ASTField> fields = new ArrayList<>(object.fields().all());
        fields.sort(Comparator.comparing(f -> f.symbol().name()));
        PrettyNode fieldsNode = new PrettyNode("Fields");
        if (fields.isEmpty()) {
            fieldsNode.add(new PrettyNode("(none)"));
        } else {
            for (ASTField field : fields) {
                fieldsNode.add(
                        new PrettyNode(
                                "Field " + field.symbol().name() + " : " + formatType(field.symbol().lpcType())));
            }
        }
        objectNode.add(fieldsNode);

        List<ASTMethod> methods = new ArrayList<>(object.methods().all());
        methods.sort(Comparator.comparing(m -> m.symbol().name()));
        PrettyNode methodsNode = new PrettyNode("Methods");
        if (methods.isEmpty()) {
            methodsNode.add(new PrettyNode("(none)"));
        } else {
            for (ASTMethod method : methods) {
                methodsNode.add(buildMethodNode(method));
            }
        }
        objectNode.add(methodsNode);

        return objectNode;
    }

    private PrettyNode buildMethodNode(ASTMethod method) {
        String returnType = formatType(method.symbol().lpcType());
        PrettyNode methodNode = new PrettyNode("FunctionDef " + method.symbol().name() + " : " + returnType);

        Set<Symbol> parameterSymbols = parameterSymbols(method.parameters());

        PrettyNode paramsNode = new PrettyNode("Params");
        if (method.parameters() == null || method.parameters().size() == 0) {
            paramsNode.add(new PrettyNode("(none)"));
        } else {
            for (ASTParameter parameter : method.parameters()) {
                paramsNode.add(
                        new PrettyNode(
                                "Param " + parameter.symbol().name() + " : " + formatType(parameter.symbol().lpcType())));
            }
        }
        methodNode.add(paramsNode);

        List<ASTLocal> locals =
                method.locals().stream().filter(local -> !parameterSymbols.contains(local.symbol())).toList();
        PrettyNode localsNode = new PrettyNode("Locals");
        if (locals.isEmpty()) {
            localsNode.add(new PrettyNode("(none)"));
        } else {
            for (ASTLocal local : locals) {
                localsNode.add(
                        new PrettyNode(
                                "Local "
                                        + local.symbol().name()
                                        + " : "
                                        + formatType(local.symbol().lpcType())
                                        + " (slot="
                                        + local.slot()
                                        + ", depth="
                                        + local.scopeDepth()
                                        + ")"));
            }
        }
        methodNode.add(localsNode);

        PrettyNode bodyNode = new PrettyNode("Body");
        if (method.body() == null || method.body().size() == 0) {
            bodyNode.add(new PrettyNode("(empty)"));
        } else {
            bodyNode.add(blockNode(method.body()));
        }
        methodNode.add(bodyNode);

        return methodNode;
    }

    private PrettyNode blockNode(ASTStmtBlock block) {
        PrettyNode node = new PrettyNode("Block");
        if (block.size() == 0) {
            node.add(new PrettyNode("(empty)"));
            return node;
        }

        for (ASTStatement statement : block) {
            node.add(statementNode(statement));
        }
        return node;
    }

    private PrettyNode statementNode(ASTStatement statement) {
        if (statement instanceof ASTStmtBlock stmtBlock)
            return blockNode(stmtBlock);

        if (statement instanceof ASTStmtExpression stmtExpression)
            return expressionNode(stmtExpression.expression());

        if (statement instanceof ASTStmtIfThenElse stmtIf) {
            PrettyNode node = new PrettyNode("If");
            node.add(taggedChild("Condition", expressionNode(stmtIf.condition())));
            node.add(taggedChild("Then", statementNode(stmtIf.thenBranch())));

            if (stmtIf.elseBranch() != null)
                node.add(taggedChild("Else", statementNode(stmtIf.elseBranch())));

            return node;
        }

        if (statement instanceof ASTStmtReturn stmtReturn) {
            String label =
                    (stmtReturn.returnValue() != null)
                            ? "Return : " + formatType(stmtReturn.returnValue().lpcType())
                            : "Return (void)";
            PrettyNode node = new PrettyNode(label);
            if (stmtReturn.returnValue() != null)
                node.add(expressionNode(stmtReturn.returnValue()));
            return node;
        }

        return new PrettyNode(statement.getClass().getSimpleName());
    }

    private PrettyNode expressionNode(ASTExpression expression) {
        if (expression == null)
            return new PrettyNode("<null expression>");

        if (expression instanceof ASTExprUnresolvedIdentifier unresolvedIdentifier) {
            return new PrettyNode("UnresolvedRef " + unresolvedIdentifier.name());
        }

        if (expression instanceof ASTExprUnresolvedAssignment unresolvedAssignment) {
            return taggedChild(
                    "UnresolvedAssign " + unresolvedAssignment.name(),
                    expressionNode(unresolvedAssignment.value()));
        }

        if (expression instanceof ASTExprUnresolvedCall unresolvedCall) {
            return new PrettyNode("UnresolvedCall " + unresolvedCall.name(), argumentNodes(unresolvedCall.arguments()));
        }

        if (expression instanceof ASTExprUnresolvedInvoke unresolvedInvoke) {
            String label = "UnresolvedInvoke " + unresolvedInvoke.targetName() + " -> " + unresolvedInvoke.methodName();
            return new PrettyNode(label, argumentNodes(unresolvedInvoke.arguments()));
        }

        if (expression instanceof ASTExprLocalStore store) {
            return taggedChild(
                    "Assign " + store.local().symbol().name() + " : " + formatType(store.lpcType()),
                    expressionNode(store.value()));
        }

        if (expression instanceof ASTExprFieldStore store) {
            return taggedChild(
                    "Assign field " + store.field().symbol().name() + " : " + formatType(store.lpcType()),
                    expressionNode(store.value()));
        }

        if (expression instanceof ASTExprLocalAccess access) {
            return new PrettyNode("VarRef " + access.local().symbol().name() + " : " + formatType(access.lpcType()));
        }

        if (expression instanceof ASTExprFieldAccess access) {
            return new PrettyNode("FieldRef " + access.field().symbol().name() + " : " + formatType(access.lpcType()));
        }

        if (expression instanceof ASTExprOpBinary opBinary) {
            return new PrettyNode(
                    "BinaryExpr " + operatorSymbol(opBinary.operator()) + " : " + formatType(opBinary.lpcType()),
                    List.of(expressionNode(opBinary.left()), expressionNode(opBinary.right())));
        }

        if (expression instanceof ASTExprOpUnary opUnary) {
            return taggedChild(
                    "UnaryExpr " + operatorSymbol(opUnary.operator()) + " : " + formatType(opUnary.lpcType()),
                    expressionNode(opUnary.right()));
        }

        if (expression instanceof ASTExprCallEfun callEfun) {
            String label = "Call efun " + callEfun.signature().name() + " : " + formatType(callEfun.lpcType());
            return new PrettyNode(label, argumentNodes(callEfun.arguments()));
        }

        if (expression instanceof ASTExprCallMethod callMethod) {
            String owner = (callMethod.method().ownerName() != null) ? callMethod.method().ownerName() : "<local>";
            String prefix = callMethod.isParentDispatch() ? "Call parent method " : "Call method ";
            String label =
                    prefix + owner + "::" + callMethod.method().symbol().name() + " : " + formatType(callMethod.lpcType());
            return new PrettyNode(label, argumentNodes(callMethod.arguments()));
        }

        if (expression instanceof ASTExprInvokeLocal invokeLocal) {
            String label =
                    "DynamicInvoke slot "
                            + invokeLocal.slot()
                            + " -> "
                            + invokeLocal.methodName()
                            + " : "
                            + formatType(invokeLocal.lpcType());
            return new PrettyNode(label, argumentNodes(invokeLocal.arguments()));
        }

        if (expression instanceof ASTExprLiteralInteger literal) {
            return new PrettyNode("IntLiteral " + literal.value());
        }

        if (expression instanceof ASTExprLiteralString literal) {
            return new PrettyNode("StringLiteral \"" + literal.value() + "\"");
        }

        if (expression instanceof ASTExprLiteralTrue) {
            return new PrettyNode("StatusLiteral true");
        }

        if (expression instanceof ASTExprLiteralFalse) {
            return new PrettyNode("StatusLiteral false");
        }

        if (expression instanceof ASTExprNull) {
            return new PrettyNode("NullLiteral");
        }

        return new PrettyNode(expression.getClass().getSimpleName());
    }

    private List<PrettyNode> argumentNodes(ASTArguments arguments) {
        if (arguments == null || arguments.size() == 0)
            return List.of(new PrettyNode("(no arguments)"));

        List<PrettyNode> nodes = new ArrayList<>();
        for (int i = 0; i < arguments.size(); i++) {
            ASTArgument argument = arguments.get(i);
            nodes.add(taggedChild("Arg " + i, expressionNode(argument.expression())));
        }
        return nodes;
    }

    private Set<Symbol> parameterSymbols(ASTParameters parameters) {
        if (parameters == null || parameters.size() == 0)
            return Set.of();

        return parameters.nodes().stream().map(ASTParameter::symbol).collect(Collectors.toCollection(HashSet::new));
    }

    private String operatorSymbol(BinaryOpType operator) {
        return switch (operator) {
        case BOP_ADD -> "+";
        case BOP_SUB -> "-";
        case BOP_MULT -> "*";
        case BOP_DIV -> "/";
        case BOP_GT -> ">";
        case BOP_LT -> "<";
        case BOP_EQ -> "==";
        case BOP_NE -> "!=";
        case BOP_GE -> ">=";
        case BOP_LE -> "<=";
        case BOP_OR -> "||";
        case BOP_AND -> "&&";
        };
    }

    private String operatorSymbol(UnaryOpType operator) {
        return switch (operator) {
        case UOP_NEGATE -> "-";
        case UOP_NOT -> "!";
        };
    }

    private String formatType(LPCType type) {
        if (type == null)
            return "<unknown>";

        return switch (type) {
        case LPCINT -> "int";
        case LPCFLOAT -> "float";
        case LPCMAPPING -> "mapping";
        case LPCMIXED -> "mixed";
        case LPCNULL -> "null";
        case LPCOBJECT -> "object";
        case LPCSTATUS -> "status";
        case LPCSTRING -> "string";
        case LPCARRAY -> "array";
        case LPCVOID -> "void";
        };
    }

    private PrettyNode taggedChild(String label, PrettyNode child) {
        return new PrettyNode(label, List.of(child));
    }

    private void printNode(PrettyNode node, String prefix, boolean isLast, boolean isRoot) {
        String connector = isRoot ? "" : (isLast ? "└─ " : "├─ ");
        out.println(prefix + connector + node.label());

        String childPrefix = prefix + (isRoot ? "" : (isLast ? "   " : "│  "));
        for (int i = 0; i < node.children().size(); i++) {
            PrettyNode child = node.children().get(i);
            printNode(child, childPrefix, i == node.children().size() - 1, false);
        }
    }

    private static final class PrettyNode {
        private final String label;
        private final List<PrettyNode> children = new ArrayList<>();

        private PrettyNode(String label) {
            this.label = label;
        }

        private PrettyNode(String label, List<PrettyNode> children) {
            this.label = label;
            if (children != null)
                this.children.addAll(children);
        }

        public void add(PrettyNode child) {
            children.add(child);
        }

        public String label() {
            return label;
        }

        public List<PrettyNode> children() {
            return children;
        }
    }
}
