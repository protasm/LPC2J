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
import io.github.protasm.lpc2j.parser.ast.ASTParameters;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.Symbol;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallEfun;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprCallMethod;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayLiteral;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprArrayStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprFieldStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeField;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprInvokeLocal;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralFalse;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralInteger;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralString;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLiteralTrue;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalAccess;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprLocalStore;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprMappingEntry;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprMappingLiteral;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprNull;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpBinary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprOpUnary;
import io.github.protasm.lpc2j.parser.ast.expr.ASTExprTernary;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBreak;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtFor;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;
import io.github.protasm.lpc2j.runtime.RuntimeType;
import io.github.protasm.lpc2j.runtime.RuntimeTypes;
import io.github.protasm.lpc2j.runtime.RuntimeValueKind;
import io.github.protasm.lpc2j.semantic.SemanticModel;
import io.github.protasm.lpc2j.semantic.SemanticScope;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
        String objectInternalName = astObject.name();
        String parentInternalName =
                (astObject.parentName() != null) ? astObject.parentName() : defaultParentInternalName;
        SemanticScope objectScope = semanticModel.objectScope();
        SemanticScope parentScope = (objectScope != null) ? objectScope.parent() : null;

        Map<Symbol, IRField> fieldsBySymbol = new HashMap<>();
        importInheritedFields(parentScope, fieldsBySymbol);
        List<IRField> fields = lowerFields(astObject.fields(), fieldsBySymbol, problems, objectInternalName);
        List<IRMethod> methods = lowerMethods(astObject, fieldsBySymbol, problems, objectInternalName);

        TypedIR typedIr = new TypedIR(new IRObject(astObject.line(), astObject.name(), parentInternalName, fields, methods));

        return new IRLoweringResult(typedIr, problems);
    }

    private List<IRField> lowerFields(
            Iterable<ASTField> astFields,
            Map<Symbol, IRField> fieldsBySymbol,
            List<CompilationProblem> problems,
            String ownerInternalName) {
        List<IRField> fields = new ArrayList<>();

        for (ASTField field : astFields) {
            RuntimeType fieldType = runtimeType(field.symbol().lpcType());
            IRExpression initializer =
                    lowerFieldInitializer(field.initializer(), fieldType, fieldsBySymbol, problems, ownerInternalName);
            IRField irField = new IRField(
                    field.line(), ownerInternalName, field.symbol().name(), fieldType, initializer);
            fields.add(irField);
            fieldsBySymbol.put(field.symbol(), irField);
        }

        return fields;
    }

    private IRExpression lowerFieldInitializer(
            ASTExpression initializer,
            RuntimeType fieldType,
            Map<Symbol, IRField> fieldsBySymbol,
            List<CompilationProblem> problems,
            String ownerInternalName) {
        if (initializer == null)
            return null;

        MethodContext context = new MethodContext(fieldType, fieldsBySymbol, ownerInternalName);
        IRExpression lowered = lowerExpression(initializer, context, problems);
        return coerceIfNeeded(lowered, fieldType);
    }

    private void importInheritedFields(SemanticScope scope, Map<Symbol, IRField> fieldsBySymbol) {
        if (scope == null)
            return;

        for (List<SemanticScope.ScopedSymbol> scopedSymbols : scope.symbols().values()) {
            for (SemanticScope.ScopedSymbol scopedSymbol : scopedSymbols) {
                if (scopedSymbol == null || scopedSymbol.field() == null)
                    continue;

                Symbol symbol = scopedSymbol.symbol();
                if (fieldsBySymbol.containsKey(symbol))
                    continue;

                RuntimeType type = runtimeType(symbol.lpcType());
                String ownerInternalName = scopedSymbol.field().ownerName();
                IRField inheritedField = new IRField(
                        scopedSymbol.field().line(),
                        (ownerInternalName != null) ? ownerInternalName : defaultParentInternalName,
                        symbol.name(),
                        type,
                        null);
                fieldsBySymbol.put(symbol, inheritedField);
            }
        }

        importInheritedFields(scope.parent(), fieldsBySymbol);
    }

    private List<IRMethod> lowerMethods(
            ASTObject astObject,
            Map<Symbol, IRField> fieldsBySymbol,
            List<CompilationProblem> problems,
            String objectInternalName) {
        List<IRMethod> methods = new ArrayList<>();

        for (ASTMethod method : astObject.methods())
            methods.add(lowerMethod(method, fieldsBySymbol, problems, objectInternalName));

        return methods;
    }

    private IRMethod lowerMethod(
            ASTMethod method,
            Map<Symbol, IRField> fieldsBySymbol,
            List<CompilationProblem> problems,
            String objectInternalName) {
        MethodContext context =
                new MethodContext(runtimeType(method.symbol().lpcType()), fieldsBySymbol, objectInternalName);

        lowerParameters(method, context);
        lowerLocals(method, context);

        BlockBuilder entryBlock = context.newBlock("entry");
        BlockBuilder tail = lowerStatement(method.body(), entryBlock, context, problems);

        if (tail != null && !tail.isTerminated())
            tail.terminate(new IRReturn(method.line(), defaultReturnExpression(method.line(), context.returnType)));

        List<IRBlock> blocks = context.buildBlocks(new IRReturn(method.line(), defaultReturnExpression(method.line(), context.returnType)));
        boolean overridesParent = method.overrides() != null;
        String overriddenOwnerInternalName = (method.overrides() != null) ? method.overrides().ownerName() : null;

        return new IRMethod(
                method.line(),
                method.symbol().name(),
                context.returnType,
                context.parameters,
                context.locals,
                blocks,
                entryBlock.label(),
                overridesParent,
                overriddenOwnerInternalName);
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

    private boolean isParameterSymbol(Symbol symbol, ASTParameters parameters) {
        if (symbol == null || parameters == null)
            return false;

        for (ASTParameter parameter : parameters) {
            if (parameter.symbol() == symbol)
                return true;
        }

        return false;
    }

    private void lowerLocals(ASTMethod method, MethodContext context) {
        int nextSlot = context.parameters.size() + 1; // include "this"

        for (ASTLocal local : method.locals()) {
            if (isParameterSymbol(local.symbol(), method.parameters()))
                continue;

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
            for (ASTStatement nested : block) {
                if (current == null)
                    break;
                current = lowerStatement(nested, current, context, problems);
            }

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

        if (statement instanceof ASTStmtFor forStmt) {
            return lowerForStatement(forStmt, current, context, problems);
        }

        if (statement instanceof ASTStmtBreak) {
            String breakTarget = context.currentBreakTarget();
            if (breakTarget == null) {
                problems.add(
                        new CompilationProblem(
                                CompilationStage.LOWER,
                                "Encountered break statement outside of a loop.",
                                statement.line()));
                return current;
            }

            current.terminate(new IRJump(statement.line(), breakTarget));
            return null;
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

    private BlockBuilder lowerForStatement(
            ASTStmtFor forStmt,
            BlockBuilder current,
            MethodContext context,
            List<CompilationProblem> problems) {
        if (forStmt.initializer() != null) {
            IRExpression initExpression = lowerExpression(forStmt.initializer(), context, problems);
            current.addStatement(new IRExpressionStatement(forStmt.line(), initExpression));
        }

        BlockBuilder conditionBlock = context.newBlock("for_cond");
        BlockBuilder bodyBlock = context.newBlock("for_body");
        BlockBuilder mergeBlock = context.newBlock("for_end");
        BlockBuilder updateBlock = (forStmt.update() != null) ? context.newBlock("for_update") : null;

        current.terminate(new IRJump(forStmt.line(), conditionBlock.label()));

        if (forStmt.condition() != null) {
            IRExpression condition = coerceIfNeeded(
                    lowerExpression(forStmt.condition(), context, problems), RuntimeTypes.STATUS);
            conditionBlock.terminate(
                    new IRConditionalJump(forStmt.line(), condition, bodyBlock.label(), mergeBlock.label()));
        } else {
            conditionBlock.terminate(new IRJump(forStmt.line(), bodyBlock.label()));
        }

        context.pushLoop(mergeBlock.label());
        BlockBuilder bodyTail = lowerStatement(forStmt.body(), bodyBlock, context, problems);
        context.popLoop();
        if (bodyTail != null && !bodyTail.isTerminated()) {
            String nextLabel = (updateBlock != null) ? updateBlock.label() : conditionBlock.label();
            bodyTail.terminate(new IRJump(forStmt.line(), nextLabel));
        }

        if (updateBlock != null) {
            IRExpression updateExpression = lowerExpression(forStmt.update(), context, problems);
            updateBlock.addStatement(new IRExpressionStatement(forStmt.line(), updateExpression));
            updateBlock.terminate(new IRJump(forStmt.line(), conditionBlock.label()));
        }

        return mergeBlock;
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

        if (expression instanceof ASTExprArrayLiteral arrayLiteral) {
            List<IRExpression> elements = new ArrayList<>();
            for (ASTExpression element : arrayLiteral.elements())
                elements.add(lowerExpression(element, context, problems));
            return new IRArrayLiteral(
                    arrayLiteral.line(), elements, RuntimeTypes.arrayOf(RuntimeTypes.MIXED));
        }

        if (expression instanceof ASTExprMappingLiteral mappingLiteral) {
            List<IRMappingEntry> entries = new ArrayList<>();
            for (ASTExprMappingEntry entry : mappingLiteral.entries()) {
                IRExpression key = coerceIfNeeded(lowerExpression(entry.key(), context, problems), RuntimeTypes.STRING);
                IRExpression value = lowerExpression(entry.value(), context, problems);
                entries.add(new IRMappingEntry(key, value));
            }
            return new IRMappingLiteral(mappingLiteral.line(), entries, RuntimeTypes.MAPPING);
        }

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

        if (expression instanceof ASTExprArrayAccess arrayAccess) {
            IRExpression target = lowerExpression(arrayAccess.target(), context, problems);
            RuntimeType targetType = runtimeType(arrayAccess.target().lpcType());
            if (targetType != null && targetType.kind() == RuntimeValueKind.MAPPING) {
                IRExpression key =
                        coerceIfNeeded(lowerExpression(arrayAccess.index(), context, problems), RuntimeTypes.STRING);
                return new IRMappingGet(arrayAccess.line(), target, key, RuntimeTypes.MIXED);
            }

            IRExpression index = coerceIfNeeded(
                    lowerExpression(arrayAccess.index(), context, problems), RuntimeTypes.INT);
            return new IRArrayGet(arrayAccess.line(), target, index, RuntimeTypes.MIXED);
        }

        if (expression instanceof ASTExprArrayStore arrayStore) {
            IRExpression target = lowerExpression(arrayStore.target(), context, problems);
            RuntimeType targetType = runtimeType(arrayStore.target().lpcType());
            if (targetType != null && targetType.kind() == RuntimeValueKind.MAPPING) {
                IRExpression key =
                        coerceIfNeeded(lowerExpression(arrayStore.index(), context, problems), RuntimeTypes.STRING);
                IRExpression value = lowerExpression(arrayStore.value(), context, problems);
                return new IRMappingSet(
                        arrayStore.line(), target, key, coerceIfNeeded(value, RuntimeTypes.MIXED), value.type());
            }

            IRExpression index = coerceIfNeeded(
                    lowerExpression(arrayStore.index(), context, problems), RuntimeTypes.INT);
            IRExpression value = lowerExpression(arrayStore.value(), context, problems);
            return new IRArraySet(
                    arrayStore.line(), target, index, coerceIfNeeded(value, RuntimeTypes.MIXED), value.type());
        }

        if (expression instanceof ASTExprOpUnary unary) {
            RuntimeType type = (unary.operator() == UnaryOpType.UOP_NOT)
                    ? RuntimeTypes.STATUS
                    : runtimeType(unary.lpcType());
            IRExpression operand = lowerExpression(unary.right(), context, problems);
            return new IRUnaryOperation(unary.line(), unary.operator(), operand, type);
        }

        if (expression instanceof ASTExprOpBinary binary) {
            if (binary.operator() == io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_ADD
                    && binary.lpcType() == LPCType.LPCARRAY) {
                IRExpression left = lowerExpression(binary.left(), context, problems);
                IRExpression right = lowerExpression(binary.right(), context, problems);
                return new IRArrayConcat(binary.line(), left, right, RuntimeTypes.arrayOf(RuntimeTypes.MIXED));
            }
            if (binary.operator() == io.github.protasm.lpc2j.parser.type.BinaryOpType.BOP_ADD
                    && binary.lpcType() == LPCType.LPCMAPPING) {
                IRExpression left = lowerExpression(binary.left(), context, problems);
                IRExpression right = lowerExpression(binary.right(), context, problems);
                return new IRMappingMerge(binary.line(), left, right, RuntimeTypes.MAPPING);
            }
            RuntimeType type = runtimeType(binary.lpcType());
            IRExpression left = lowerExpression(binary.left(), context, problems);
            IRExpression right = lowerExpression(binary.right(), context, problems);
            return new IRBinaryOperation(binary.line(), binary.operator(), left, right, type);
        }

        if (expression instanceof ASTExprTernary ternary) {
            RuntimeType targetType = runtimeType(ternary.lpcType());
            IRExpression condition = coerceIfNeeded(
                    lowerExpression(ternary.condition(), context, problems), RuntimeTypes.STATUS);
            IRExpression thenBranch =
                    coerceIfNeeded(lowerExpression(ternary.thenBranch(), context, problems), targetType);
            IRExpression elseBranch =
                    coerceIfNeeded(lowerExpression(ternary.elseBranch(), context, problems), targetType);
            return new IRConditionalExpression(ternary.line(), condition, thenBranch, elseBranch, targetType);
        }

        if (expression instanceof ASTExprCallEfun callEfun) {
            List<IRExpression> args = lowerArguments(callEfun.arguments(), context, problems);
            RuntimeType returnType = runtimeType(callEfun.lpcType());
            return new IREfunCall(callEfun.line(), callEfun.signature().name(), args, returnType);
        }

        if (expression instanceof ASTExprCallMethod callMethod) {
            List<IRExpression> args = lowerArguments(callMethod.arguments(), context, problems);
            RuntimeType returnType = runtimeType(callMethod.lpcType());
            String ownerInternalName = callMethod.method().ownerName();
            if (ownerInternalName == null && !callMethod.isParentDispatch())
                ownerInternalName = defaultParentInternalName;
            List<RuntimeType> parameterTypes = parameterTypes(callMethod.method());
            return new IRInstanceCall(
                    callMethod.line(),
                    ownerInternalName,
                    callMethod.method().symbol().name(),
                    callMethod.isParentDispatch(),
                    args,
                    parameterTypes,
                    returnType);
        }

        if (expression instanceof ASTExprInvokeLocal invokeLocal) {
            IRLocal target = context.requireLocal(invokeLocal.local(), problems);
            List<IRExpression> args = lowerArguments(invokeLocal.args(), context, problems);
            RuntimeType returnType = runtimeType(invokeLocal.lpcType());
            return new IRDynamicInvoke(invokeLocal.line(), target, invokeLocal.methodName(), args, returnType);
        }

        if (expression instanceof ASTExprInvokeField invokeField) {
            IRField target = context.requireField(invokeField.field(), problems);
            List<IRExpression> args = lowerArguments(invokeField.args(), context, problems);
            RuntimeType returnType = runtimeType(invokeField.lpcType());
            return new IRDynamicInvokeField(invokeField.line(), target, invokeField.methodName(), args, returnType);
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

    private List<RuntimeType> parameterTypes(ASTMethod method) {
        List<RuntimeType> types = new ArrayList<>();

        if (method.parameters() == null)
            return types;

        for (ASTParameter parameter : method.parameters())
            types.add(runtimeType(parameter.symbol().lpcType()));

        return types;
    }

    private IRExpression coerceIfNeeded(IRExpression value, RuntimeType targetType) {
        if (value == null)
            return new IRConstant(0, null, targetType != null ? targetType : RuntimeTypes.MIXED);

        if (targetType == null || targetType.equals(value.type()))
            return value;

        if (value instanceof IRConstant constant
                && constant.type() == RuntimeTypes.INT
                && Integer.valueOf(0).equals(constant.value())) {
            return switch (targetType.kind()) {
            case STRING -> new IRConstant(value.line(), "", RuntimeTypes.STRING);
            case STATUS -> new IRConstant(value.line(), 0, RuntimeTypes.STATUS);
            case OBJECT -> new IRConstant(value.line(), null, RuntimeTypes.OBJECT);
            case ARRAY -> new IRArrayLiteral(value.line(), List.of(), targetType);
            case MAPPING -> new IRMappingLiteral(value.line(), List.of(), targetType);
            default -> new IRCoerce(value.line(), value, targetType);
            };
        }

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
        private final String currentInternalName;
        private final Map<Symbol, IRLocal> localsBySymbol = new HashMap<>();
        private final Map<Integer, IRLocal> localsBySlot = new HashMap<>();
        private final List<IRParameter> parameters = new ArrayList<>();
        private final List<IRLocal> locals = new ArrayList<>();
        private final List<BlockBuilder> blocks = new ArrayList<>();
        private final Deque<String> breakTargets = new ArrayDeque<>();

        private int blockCounter = 0;

        private MethodContext(RuntimeType returnType, Map<Symbol, IRField> fieldsBySymbol, String currentInternalName) {
            this.returnType = returnType != null ? returnType : RuntimeTypes.MIXED;
            this.fieldsBySymbol = fieldsBySymbol;
            this.currentInternalName = currentInternalName;
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

        public void pushLoop(String breakTarget) {
            breakTargets.push(breakTarget);
        }

        public void popLoop() {
            if (!breakTargets.isEmpty())
                breakTargets.pop();
        }

        public String currentBreakTarget() {
            return breakTargets.peek();
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
            String owner = (astField.ownerName() != null) ? astField.ownerName() : currentInternalName;
            IRField synthesized = new IRField(astField.line(), owner, astField.symbol().name(), type, null);
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
