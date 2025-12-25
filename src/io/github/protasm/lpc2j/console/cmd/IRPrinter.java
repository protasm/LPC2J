package io.github.protasm.lpc2j.console.cmd;

import io.github.protasm.lpc2j.ir.IRBinaryOperation;
import io.github.protasm.lpc2j.ir.IRBlock;
import io.github.protasm.lpc2j.ir.IRCoerce;
import io.github.protasm.lpc2j.ir.IRConditionalExpression;
import io.github.protasm.lpc2j.ir.IRConditionalJump;
import io.github.protasm.lpc2j.ir.IRConstant;
import io.github.protasm.lpc2j.ir.IRDynamicInvoke;
import io.github.protasm.lpc2j.ir.IRDynamicInvokeField;
import io.github.protasm.lpc2j.ir.IREfunCall;
import io.github.protasm.lpc2j.ir.IRExpression;
import io.github.protasm.lpc2j.ir.IRExpressionStatement;
import io.github.protasm.lpc2j.ir.IRField;
import io.github.protasm.lpc2j.ir.IRFieldLoad;
import io.github.protasm.lpc2j.ir.IRFieldStore;
import io.github.protasm.lpc2j.ir.IRInstanceCall;
import io.github.protasm.lpc2j.ir.IRJump;
import io.github.protasm.lpc2j.ir.IRLocal;
import io.github.protasm.lpc2j.ir.IRLocalLoad;
import io.github.protasm.lpc2j.ir.IRLocalStore;
import io.github.protasm.lpc2j.ir.IRMethod;
import io.github.protasm.lpc2j.ir.IRObject;
import io.github.protasm.lpc2j.ir.IRParameter;
import io.github.protasm.lpc2j.ir.IRReturn;
import io.github.protasm.lpc2j.ir.IRStatement;
import io.github.protasm.lpc2j.ir.IRTerminator;
import io.github.protasm.lpc2j.ir.IRUnaryOperation;
import io.github.protasm.lpc2j.ir.TypedIR;
import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;
import io.github.protasm.lpc2j.runtime.RuntimeType;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

final class IRPrinter {
    private final PrintStream out;

    IRPrinter(PrintStream out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    void print(TypedIR typedIr) {
        if (typedIr == null) {
            out.println("IR: (none)");
            return;
        }

        IRObject object = typedIr.object();
        out.printf("IR Object %s (parent=%s)%n", object.name(), object.parentInternalName());
        printFields("  ", object.fields());
        printMethods("  ", object.methods());
    }

    private void printFields(String indent, List<IRField> fields) {
        println(indent, "Fields:");
        if (fields.isEmpty()) {
            println(indent + "  ", "(none)");
            return;
        }

        for (IRField field : fields) {
            println(
                    indent + "  ",
                    field.name() + " : " + typeLabel(field.type()) + " (line " + field.line() + ")");
        }
    }

    private void printMethods(String indent, List<IRMethod> methods) {
        println(indent, "Methods:");
        if (methods.isEmpty()) {
            println(indent + "  ", "(none)");
            return;
        }

        for (IRMethod method : methods) {
            printMethod(indent + "  ", method);
        }
    }

    private void printMethod(String indent, IRMethod method) {
        println(
                indent,
                method.name()
                        + " : "
                        + typeLabel(method.returnType())
                        + " (entry="
                        + method.entryBlockLabel()
                        + ", line "
                        + method.line()
                        + ")");
        printParameters(indent + "  ", method.parameters());
        printLocals(indent + "  ", method.locals());
        printBlocks(indent + "  ", method.blocks(), method.entryBlockLabel());
    }

    private void printParameters(String indent, List<IRParameter> parameters) {
        println(indent, "Params:");
        if (parameters.isEmpty()) {
            println(indent + "  ", "(none)");
            return;
        }

        for (IRParameter parameter : parameters) {
            println(
                    indent + "  ",
                    parameter.name()
                            + " : "
                            + typeLabel(parameter.type())
                            + " (slot "
                            + parameter.local().slot()
                            + ")");
        }
    }

    private void printLocals(String indent, List<IRLocal> locals) {
        List<IRLocal> nonParams = locals.stream().filter(local -> !local.parameter()).toList();
        println(indent, "Locals:");
        if (nonParams.isEmpty()) {
            println(indent + "  ", "(none)");
            return;
        }

        for (IRLocal local : nonParams) {
            println(
                    indent + "  ",
                    renderLocal(local) + " (slot " + local.slot() + ", line " + local.line() + ")");
        }
    }

    private void printBlocks(String indent, List<IRBlock> blocks, String entryLabel) {
        println(indent, "Blocks:");
        if (blocks.isEmpty()) {
            println(indent + "  ", "(none)");
            return;
        }

        for (IRBlock block : blocks) {
            String label = block.label().equals(entryLabel) ? block.label() + " [entry]" : block.label();
            println(indent + "  ", label + ":");
            String stmtIndent = indent + "    ";
            for (IRStatement statement : block.statements()) {
                println(stmtIndent, renderStatement(statement));
            }
            println(stmtIndent, renderTerminator(block.terminator()));
        }
    }

    private String renderStatement(IRStatement statement) {
        if (statement instanceof IRExpressionStatement exprStmt) {
            return renderExpression(exprStmt.expression());
        }

        if (statement instanceof IRTerminator terminator) {
            return renderTerminator(terminator);
        }

        return statement.getClass().getSimpleName();
    }

    private String renderTerminator(IRTerminator terminator) {
        if (terminator instanceof IRReturn irReturn) {
            return (irReturn.returnValue() != null)
                    ? "return " + renderExpression(irReturn.returnValue())
                    : "return";
        }

        if (terminator instanceof IRJump jump) {
            return "jump " + jump.targetLabel();
        }

        if (terminator instanceof IRConditionalJump condJump) {
            return "if " + renderExpression(condJump.condition()) + " goto " + condJump.trueLabel()
                    + " else " + condJump.falseLabel();
        }

        return terminator.getClass().getSimpleName();
    }

    private String renderExpression(IRExpression expression) {
        if (expression == null)
            return "<null>";

        if (expression instanceof IRConstant constant) {
            return "const " + valueLabel(constant.value()) + " : " + typeLabel(constant.type());
        }

        if (expression instanceof IRLocalLoad load) {
            return "load " + renderLocal(load.local());
        }

        if (expression instanceof IRLocalStore store) {
            return "store " + renderLocal(store.local()) + " = " + renderExpression(store.value());
        }

        if (expression instanceof IRFieldLoad fieldLoad) {
            return "load field " + fieldLoad.field().name() + " : " + typeLabel(fieldLoad.field().type());
        }

        if (expression instanceof IRFieldStore fieldStore) {
            return "store field "
                    + fieldStore.field().name()
                    + " = "
                    + renderExpression(fieldStore.value())
                    + " : "
                    + typeLabel(fieldStore.field().type());
        }

        if (expression instanceof IRUnaryOperation unary) {
            return unarySymbol(unary.operator()) + renderExpression(unary.operand()) + " : " + typeLabel(unary.type());
        }

        if (expression instanceof IRBinaryOperation binary) {
            return "(" + renderExpression(binary.left()) + " " + binarySymbol(binary.operator()) + " "
                    + renderExpression(binary.right()) + ") : " + typeLabel(binary.type());
        }

        if (expression instanceof IRConditionalExpression conditional) {
            return "("
                    + renderExpression(conditional.condition())
                    + " ? "
                    + renderExpression(conditional.thenBranch())
                    + " : "
                    + renderExpression(conditional.elseBranch())
                    + ") : "
                    + typeLabel(conditional.type());
        }

        if (expression instanceof IREfunCall efunCall) {
            return "efun " + efunCall.name() + "(" + renderArgs(efunCall.arguments()) + ") : "
                    + typeLabel(efunCall.type());
        }

        if (expression instanceof IRInstanceCall instanceCall) {
            String owner =
                    (instanceCall.ownerInternalName() != null && !instanceCall.ownerInternalName().isEmpty())
                            ? instanceCall.ownerInternalName()
                            : "<local>";
            String dispatch = instanceCall.parentDispatch() ? "parent_call " : "call ";
            return dispatch + owner + "::" + instanceCall.methodName() + "(" + renderArgs(instanceCall.arguments())
                    + ") : " + typeLabel(instanceCall.type());
        }

        if (expression instanceof IRDynamicInvoke invoke) {
            return "dynamic_invoke "
                    + renderLocal(invoke.targetLocal())
                    + "."
                    + invoke.methodName()
                    + "("
                    + renderArgs(invoke.arguments())
                    + ") : "
                    + typeLabel(invoke.type());
        }

        if (expression instanceof IRDynamicInvokeField invoke) {
            return "dynamic_invoke "
                    + invoke.targetField().name()
                    + "."
                    + invoke.methodName()
                    + "("
                    + renderArgs(invoke.arguments())
                    + ") : "
                    + typeLabel(invoke.type());
        }

        if (expression instanceof IRCoerce coerce) {
            return "coerce " + renderExpression(coerce.value()) + " -> " + typeLabel(coerce.targetType());
        }

        return expression.getClass().getSimpleName();
    }

    private String renderLocal(IRLocal local) {
        return "%" + local.slot() + "(" + local.name() + ") : " + typeLabel(local.type());
    }

    private String renderArgs(List<IRExpression> args) {
        return args.stream().map(this::renderExpression).collect(Collectors.joining(", "));
    }

    private String typeLabel(RuntimeType type) {
        if (type == null)
            return "<unknown>";

        String descriptor = type.descriptor();
        return type.kind().name().toLowerCase() + (descriptor != null ? " [" + descriptor + "]" : "");
    }

    private String valueLabel(Object value) {
        if (value == null)
            return "null";
        if (value instanceof String s)
            return "\"" + s + "\"";
        if (value instanceof Character c)
            return "'" + c + "'";
        return value.toString();
    }

    private String binarySymbol(BinaryOpType op) {
        return switch (op) {
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

    private String unarySymbol(UnaryOpType op) {
        return switch (op) {
        case UOP_NEGATE -> "-";
        case UOP_NOT -> "!";
        };
    }

    private void println(String indent, String text) {
        out.println(indent + text);
    }
}
