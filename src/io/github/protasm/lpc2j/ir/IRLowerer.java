package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.pipeline.CompilationProblem;
import io.github.protasm.lpc2j.pipeline.CompilationStage;
import io.github.protasm.lpc2j.parser.ast.ASTArgument;
import io.github.protasm.lpc2j.parser.ast.ASTArguments;
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTField;
import io.github.protasm.lpc2j.parser.ast.ASTLocal;
import io.github.protasm.lpc2j.parser.ast.ASTMethod;
import io.github.protasm.lpc2j.parser.ast.ASTObject;
import io.github.protasm.lpc2j.parser.ast.ASTParameter;
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
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;
import io.github.protasm.lpc2j.runtime.RuntimeType;
import io.github.protasm.lpc2j.runtime.RuntimeTypes;
import io.github.protasm.lpc2j.semantic.SemanticModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Lowers typed AST nodes into the JVM-oriented IR model. */
public final class IRLowerer {
    private final String defaultParentInternalName;

    public IRLowerer(String defaultParentInternalName) {
        this.defaultParentInternalName = Objects.requireNonNull(defaultParentInternalName, "defaultParentInternalName");
    }

    public IRLoweringResult lower(SemanticModel semanticModel) {
        if (semanticModel == null)
            throw new IllegalArgumentException("SemanticModel cannot be null.");

        List<CompilationProblem> problems = new ArrayList<>();
        ASTObject astObject = semanticModel.astObject();
        String parentInternalName =
                (astObject.parentName() != null) ? astObject.parentName() : defaultParentInternalName;

        Map<Symbol, IRField> fieldsBySymbol = new HashMap<>();
        List<IRField> fields = lowerFields(astObject.fields(), fieldsBySymbol);
        List<IRMethod> methods = lowerMethods(astObject, fieldsBySymbol, problems);

        TypedIR typedIr = new TypedIR(new IRObject(astObject.line(), astObject.name(), parentInternalName, fields, methods));

        return new IRLoweringResult(typedIr, problems);
    }

    private List<IRField> lowerFields(Iterable<ASTField> astFields, Map<Symbol, IRField> fieldsBySymbol) {
        List<IRField> fields = new ArrayList<>();

        for (ASTField field : astFields) {
            RuntimeType fieldType = runtimeType(field.symbol().lpcType());
            IRField irField = new IRField(field.line(), field.symbol().name(), fieldType);
            fields.add(irField);
            fieldsBySymbol.put(field.symbol(), irField);
        }

        return fields;
    }

    private List<IRMethod> lowerMethods(
            ASTObject astObject, Map<Symbol, IRField> fieldsBySymbol, List<CompilationProblem> problems) {
        List<IRMethod> methods = new ArrayList<>();

        for (ASTMethod method : astObject.methods())
            methods.add(lowerMethod(method, fieldsBySymbol, problems));

        return methods;
    }

    private IRMethod lowerMethod(
            ASTMethod method, Map<Symbol, IRField> fieldsBySymbol, List<CompilationProblem> problems) {
        MethodContext context = new MethodContext(runtimeType(method.symbol().lpcType()), fieldsBySymbol);

        lowerParameters(method, context);
        lowerLocals(method, context);

        BlockBuilder entryBlock = context.newBlock("entry");
        BlockBuilder tail = lowerStatement(method.body(), entryBlock, context, problems);

        if (tail != null && !tail.isTerminated())
            tail.terminate(new IRReturn(method.line(), defaultReturnExpression(method.line(), context.returnType)));

        List<IRBlock> blocks = context.buildBlocks(new IRReturn(method.line(), defaultReturnExpression(method.line(), context.returnType)));

        return new IRMethod(
                method.line(),
                method.symbol().name(),
                context.returnType,
                context.parameters,
                context.locals,
                blocks,
                entryBlock.label());
    }

    private void lowerParameters(ASTMethod method, MethodContext context) {
        if (method.parameters() == null)
            return;

        int slot = 1; // slot 0 reserved for "this"
        for (ASTParameter parameter : method.parameters()) {
            RuntimeType type = runtimeType(parameter.symbol().lpcType());
            IRLocal local = new IRLocal(parameter.line(), parameter.symbol().name(), type, slot++, true);
            context.registerLocal(parameter.symbol(), local);
            context.parameters.add(new IRParameter(parameter.line(), parameter.symbol().name(), type, local));
        }
    }

    private void lowerLocals(ASTMethod method, MethodContext context) {
        int nextSlot = context.parameters.size() + 1; // include "this"

        for (ASTLocal local : method.locals()) {
            RuntimeType type = runtimeType(local.symbol().lpcType());
            int slot = (local.slot() >= 0) ? local.slot() : nextSlot++;
            IRLocal irLocal = new IRLocal(local.line(), local.symbol().name(), type, slot, false);
            context.locals.add(irLocal);
            context.registerLocal(local.symbol(), irLocal);
        }
    }

    private BlockBuilder lowerStatement(
            ASTStatement statement, BlockBuilder current, MethodContext context, List<CompilationProblem> problems) {
        if (current == null || current.isTerminated())
            return current;

        if (statement == null) {
            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER,
                            "Encountered null statement while lowering method body.",
                            (Integer) null));
            return current;
        }

        if (statement instanceof ASTStmtBlock block) {
            for (ASTStatement nested : block)
                current = lowerStatement(nested, current, context, problems);

            return current;
        }

        if (statement instanceof ASTStmtExpression stmtExpression) {
            IRExpression expression = lowerExpression(stmtExpression.expression(), context, problems);
            current.addStatement(new IRExpressionStatement(statement.line(), expression));
            return current;
        }

        if (statement instanceof ASTStmtIfThenElse ifStmt) {
            return lowerIfStatement(ifStmt, current, context, problems);
        }

        if (statement instanceof ASTStmtReturn stmtReturn) {
            IRExpression returnValue =
                    (stmtReturn.returnValue() != null)
                            ? coerceIfNeeded(
                                    lowerExpression(stmtReturn.returnValue(), context, problems), context.returnType)
                            : defaultReturnExpression(statement.line(), context.returnType);

            current.terminate(new IRReturn(statement.line(), returnValue));
            return null;
        }

        problems.add(
                new CompilationProblem(
                        CompilationStage.LOWER,
                        "Unsupported statement kind: " + statement.getClass().getSimpleName(),
                        statement.line()));
        return current;
    }

    private BlockBuilder lowerIfStatement(
            ASTStmtIfThenElse ifStmt,
            BlockBuilder current,
            MethodContext context,
            List<CompilationProblem> problems) {
        BlockBuilder thenBlock = context.newBlock("then");
        BlockBuilder mergeBlock = context.newBlock("endif");
        BlockBuilder elseBlock = (ifStmt.elseBranch() != null) ? context.newBlock("else") : mergeBlock;

        IRExpression condition = coerceIfNeeded(
                lowerExpression(ifStmt.condition(), context, problems), RuntimeTypes.STATUS);

        current.terminate(new IRConditionalJump(ifStmt.line(), condition, thenBlock.label(), elseBlock.label()));

        BlockBuilder thenTail = lowerStatement(ifStmt.thenBranch(), thenBlock, context, problems);
        if (thenTail != null && !thenTail.isTerminated())
            thenTail.terminate(new IRJump(ifStmt.line(), mergeBlock.label()));

        if (ifStmt.elseBranch() != null) {
            BlockBuilder elseTail = lowerStatement(ifStmt.elseBranch(), elseBlock, context, problems);
            if (elseTail != null && !elseTail.isTerminated())
                elseTail.terminate(new IRJump(ifStmt.line(), mergeBlock.label()));
        }

        return mergeBlock;
    }

    private IRExpression lowerExpression(
            ASTExpression expression, MethodContext context, List<CompilationProblem> problems) {
        if (expression == null)
            return new IRConstant(0, null, RuntimeTypes.NULL);

        if (expression instanceof ASTExprLiteralInteger literal)
            return new IRConstant(literal.line(), literal.value(), RuntimeTypes.INT);

        if (expression instanceof ASTExprLiteralString literal)
            return new IRConstant(literal.line(), literal.value(), RuntimeTypes.STRING);

        if (expression instanceof ASTExprLiteralTrue literal)
            return new IRConstant(literal.line(), Boolean.TRUE, RuntimeTypes.STATUS);

        if (expression instanceof ASTExprLiteralFalse literal)
            return new IRConstant(literal.line(), Boolean.FALSE, RuntimeTypes.STATUS);

        if (expression instanceof ASTExprNull literalNull)
            return new IRConstant(literalNull.line(), null, RuntimeTypes.NULL);

        if (expression instanceof ASTExprLocalAccess access)
            return new IRLocalLoad(access.line(), context.requireLocal(access.local(), problems));

        if (expression instanceof ASTExprLocalStore store) {
            IRLocal target = context.requireLocal(store.local(), problems);
            IRExpression value = coerceIfNeeded(lowerExpression(store.value(), context, problems), target.type());
            return new IRLocalStore(store.line(), target, value);
        }

        if (expression instanceof ASTExprFieldAccess fieldAccess) {
            IRField field = context.requireField(fieldAccess.field(), problems);
            return new IRFieldLoad(fieldAccess.line(), field);
        }

        if (expression instanceof ASTExprFieldStore fieldStore) {
            IRField field = context.requireField(fieldStore.field(), problems);
            IRExpression value =
                    coerceIfNeeded(lowerExpression(fieldStore.value(), context, problems), field.type());
            return new IRFieldStore(fieldStore.line(), field, value);
        }

        if (expression instanceof ASTExprOpUnary unary) {
            RuntimeType type = (unary.operator() == UnaryOpType.UOP_NOT)
                    ? RuntimeTypes.STATUS
                    : runtimeType(unary.lpcType());
            IRExpression operand = lowerExpression(unary.right(), context, problems);
            return new IRUnaryOperation(unary.line(), unary.operator(), operand, type);
        }

        if (expression instanceof ASTExprOpBinary binary) {
            RuntimeType type = runtimeType(binary.lpcType());
            IRExpression left = lowerExpression(binary.left(), context, problems);
            IRExpression right = lowerExpression(binary.right(), context, problems);
            return new IRBinaryOperation(binary.line(), binary.operator(), left, right, type);
        }

        if (expression instanceof ASTExprCallEfun callEfun) {
            List<IRExpression> args = lowerArguments(callEfun.arguments(), context, problems);
            RuntimeType returnType = runtimeType(callEfun.lpcType());
            return new IREfunCall(callEfun.line(), callEfun.signature().name(), args, returnType);
        }

        if (expression instanceof ASTExprCallMethod callMethod) {
            List<IRExpression> args = lowerArguments(callMethod.arguments(), context, problems);
            RuntimeType returnType = runtimeType(callMethod.lpcType());
            String ownerInternalName =
                    (callMethod.method().ownerName() != null) ? callMethod.method().ownerName() : defaultParentInternalName;
            return new IRInstanceCall(
                    callMethod.line(),
                    ownerInternalName,
                    callMethod.method().symbol().name(),
                    args,
                    returnType);
        }

        if (expression instanceof ASTExprInvokeLocal invokeLocal) {
            IRLocal target = context.localBySlot(invokeLocal.slot(), problems);
            List<IRExpression> args = lowerArguments(invokeLocal.args(), context, problems);
            RuntimeType returnType = runtimeType(invokeLocal.lpcType());
            return new IRDynamicInvoke(invokeLocal.line(), target, invokeLocal.methodName(), args, returnType);
        }

        problems.add(
                new CompilationProblem(
                        CompilationStage.LOWER,
                        "Unsupported expression kind: " + expression.getClass().getSimpleName(),
                        expression.line()));
        return new IRConstant(expression.line(), null, RuntimeTypes.MIXED);
    }

    private List<IRExpression> lowerArguments(
            ASTArguments arguments, MethodContext context, List<CompilationProblem> problems) {
        List<IRExpression> lowered = new ArrayList<>();

        if (arguments == null)
            return lowered;

        for (ASTArgument argument : arguments)
            lowered.add(lowerExpression(argument.expression(), context, problems));

        return lowered;
    }

    private IRExpression coerceIfNeeded(IRExpression value, RuntimeType targetType) {
        if (value == null)
            return new IRConstant(0, null, targetType != null ? targetType : RuntimeTypes.MIXED);

        if (targetType == null || targetType.equals(value.type()))
            return value;

        return new IRCoerce(value.line(), value, targetType);
    }

    private RuntimeType runtimeType(LPCType lpcType) {
        return RuntimeTypes.fromLpcType(lpcType);
    }

    private IRExpression defaultReturnExpression(int line, RuntimeType returnType) {
        if (returnType == null || returnType == RuntimeTypes.VOID)
            return null;

        return switch (returnType.kind()) {
        case INT, STATUS -> new IRConstant(line, 0, returnType);
        case FLOAT -> new IRConstant(line, 0.0f, returnType);
        case STRING, OBJECT, MAPPING, MIXED, ARRAY, EFUN, NULL -> new IRConstant(line, null, returnType);
        case VOID -> null;
        };
    }

    private static final class BlockBuilder {
        private final String label;
        private final List<IRStatement> statements = new ArrayList<>();
        private IRTerminator terminator;

        private BlockBuilder(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public void addStatement(IRStatement statement) {
            statements.add(statement);
        }

        public boolean isTerminated() {
            return terminator != null;
        }

        public void terminate(IRTerminator terminator) {
            this.terminator = terminator;
        }

        public IRBlock build(IRTerminator defaultTerminator) {
            return new IRBlock(label, statements, terminator != null ? terminator : defaultTerminator);
        }
    }

    private static final class MethodContext {
        private final RuntimeType returnType;
        private final Map<Symbol, IRField> fieldsBySymbol;
        private final Map<Symbol, IRLocal> localsBySymbol = new HashMap<>();
        private final Map<Integer, IRLocal> localsBySlot = new HashMap<>();
        private final List<IRParameter> parameters = new ArrayList<>();
        private final List<IRLocal> locals = new ArrayList<>();
        private final List<BlockBuilder> blocks = new ArrayList<>();

        private int blockCounter = 0;

        private MethodContext(RuntimeType returnType, Map<Symbol, IRField> fieldsBySymbol) {
            this.returnType = returnType != null ? returnType : RuntimeTypes.MIXED;
            this.fieldsBySymbol = fieldsBySymbol;
        }

        public BlockBuilder newBlock(String prefix) {
            BlockBuilder builder = new BlockBuilder(prefix + "_" + blockCounter++);
            blocks.add(builder);
            return builder;
        }

        public void registerLocal(Symbol symbol, IRLocal local) {
            localsBySymbol.put(symbol, local);
            localsBySlot.put(local.slot(), local);
        }

        public IRLocal requireLocal(ASTLocal astLocal, List<CompilationProblem> problems) {
            if (astLocal == null || astLocal.symbol() == null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.LOWER,
                                "Encountered null local reference during lowering.",
                                (astLocal != null) ? astLocal.line() : null));
                return new IRLocal(0, "<invalid>", RuntimeTypes.MIXED, -1, false);
            }

            IRLocal local = localsBySymbol.get(astLocal.symbol());

            if (local != null)
                return local;

            IRLocal synthesized =
                    new IRLocal(astLocal.line(), astLocal.symbol().name(), RuntimeTypes.MIXED, astLocal.slot(), false);
            registerLocal(astLocal.symbol(), synthesized);
            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER,
                            "Synthesizing missing local '" + astLocal.symbol().name() + "' during lowering.",
                            astLocal.line()));
            return synthesized;
        }

        public IRLocal localBySlot(int slot, List<CompilationProblem> problems) {
            IRLocal local = localsBySlot.get(slot);
            if (local != null)
                return local;

            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER,
                            "No local found at slot " + slot + " for dynamic invocation.",
                            (Integer) null));
            return new IRLocal(0, "<invalid>", RuntimeTypes.MIXED, slot, false);
        }

        public IRField requireField(ASTField astField, List<CompilationProblem> problems) {
            IRField field = fieldsBySymbol.get(astField.symbol());
            if (field != null)
                return field;

            RuntimeType type = RuntimeTypes.fromLpcType(astField.symbol().lpcType());
            IRField synthesized = new IRField(astField.line(), astField.symbol().name(), type);
            fieldsBySymbol.put(astField.symbol(), synthesized);
            problems.add(
                    new CompilationProblem(
                            CompilationStage.LOWER,
                            "Synthesizing missing field '" + astField.symbol().name() + "' during lowering.",
                            astField.line()));
            return synthesized;
        }

        public List<IRBlock> buildBlocks(IRTerminator defaultTerminator) {
            List<IRBlock> built = new ArrayList<>();
            for (BlockBuilder builder : blocks)
                built.add(builder.build(defaultTerminator));
            return built;
        }
    }
}
