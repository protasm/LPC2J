package io.github.protasm.lpc2j.compiler;

import static org.objectweb.asm.Opcodes.*;

import io.github.protasm.lpc2j.ir.*;
import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;
import io.github.protasm.lpc2j.runtime.RuntimeTypes;
import io.github.protasm.lpc2j.runtime.RuntimeType;
import io.github.protasm.lpc2j.runtime.RuntimeValueKind;
import io.github.protasm.lpc2j.runtime.Truth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Emits JVM bytecode from the typed IR.
 *
 * <p>This emitter assumes the IR has already been semantically validated; it does not perform
 * additional semantic checks or fallbacks.</p>
 */
public final class Compiler {
    private final String defaultParentInternalName;

    public Compiler(String defaultParentInternalName) {
        this.defaultParentInternalName =
                Objects.requireNonNull(defaultParentInternalName, "defaultParentInternalName");
    }

    public byte[] compile(TypedIR typedIr) {
        if (typedIr == null)
            throw new CompileException("TypedIR cannot be null.");

        IRObject object = typedIr.object();
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        String internalName = object.name();
        String parentName =
                (object.parentInternalName() != null) ? object.parentInternalName() : defaultParentInternalName;

        cw.visit(V21, ACC_SUPER | ACC_PUBLIC, internalName, null, parentName, null);

        emitFields(cw, object);
        emitDefaultConstructor(cw, internalName, parentName, object.fields());

        for (IRMethod method : object.methods())
            emitMethod(cw, internalName, method);

        return cw.toByteArray();
    }

    private void emitFields(ClassWriter cw, IRObject object) {
        for (IRField field : object.fields())
            cw.visitField(ACC_PRIVATE, field.name(), descriptor(field.type()), null, null).visitEnd();
    }

    private void emitDefaultConstructor(ClassWriter cw, String internalName, String parentName, List<IRField> fields) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, parentName, "<init>", "()V", false);

        for (IRField field : fields) {
            if (field.initializer() == null)
                continue;

            mv.visitVarInsn(ALOAD, 0);
            emitExpression(mv, internalName, null, field.initializer());
            mv.visitFieldInsn(PUTFIELD, internalName, field.name(), descriptor(field.type()));
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void emitMethod(ClassWriter cw, String internalName, IRMethod method) {
        String descriptor = methodDescriptor(method);
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.name(), descriptor, null, null);
        mv.visitCode();

        Map<String, Label> labels = new HashMap<>();
        for (IRBlock block : method.blocks())
            labels.put(block.label(), new Label());

        for (IRBlock block : method.blocks()) {
            mv.visitLabel(labels.get(block.label()));
            for (IRStatement statement : block.statements())
                emitStatement(mv, internalName, method, statement);

            emitTerminator(mv, internalName, method, block.terminator(), labels);
        }

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void emitStatement(MethodVisitor mv, String internalName, IRMethod method, IRStatement statement) {
        if (statement instanceof IRExpressionStatement exprStmt)
            emitExpressionStatement(mv, internalName, method, exprStmt);
    }

    private void emitExpressionStatement(
            MethodVisitor mv, String internalName, IRMethod method, IRExpressionStatement exprStmt) {
        IRExpression expression = exprStmt.expression();
        emitExpression(mv, internalName, method, expression);

        RuntimeType type = expression != null ? expression.type() : null;
        if (type != null && type.kind() != RuntimeValueKind.VOID)
            mv.visitInsn(type.jvmType() == null || type.isReferenceLike() ? POP : POP);
    }

    private void emitTerminator(
            MethodVisitor mv,
            String internalName,
            IRMethod method,
            IRTerminator terminator,
            Map<String, Label> labels) {
        if (terminator instanceof IRReturn irReturn) {
            emitReturn(mv, internalName, method, irReturn);
            return;
        }

        if (terminator instanceof IRJump irJump) {
            mv.visitJumpInsn(GOTO, labels.get(irJump.targetLabel()));
            return;
        }

        if (terminator instanceof IRConditionalJump irConditional) {
            emitConditionalJump(mv, internalName, method, irConditional, labels);
        }
    }

    private void emitConditionalJump(
            MethodVisitor mv,
            String internalName,
            IRMethod method,
            IRConditionalJump irConditional,
            Map<String, Label> labels) {
        Label trueLabel = labels.get(irConditional.trueLabel());
        Label falseLabel = labels.get(irConditional.falseLabel());

        emitBooleanValue(mv, internalName, method, irConditional.condition());
        mv.visitJumpInsn(IFNE, trueLabel);
        mv.visitJumpInsn(GOTO, falseLabel);
    }

    private void emitReturn(MethodVisitor mv, String internalName, IRMethod method, IRReturn irReturn) {
        IRExpression returnValue = irReturn.returnValue();
        RuntimeType returnType = method.returnType();

        if (returnValue != null)
            emitExpression(mv, internalName, method, returnValue);

        switch (returnType.kind()) {
        case INT, STATUS:
            mv.visitInsn(IRETURN);
            break;
        case FLOAT:
            mv.visitInsn(FRETURN);
            break;
        case VOID:
            mv.visitInsn(RETURN);
            break;
        default:
            mv.visitInsn(ARETURN);
            break;
        }
    }

    private void emitExpression(MethodVisitor mv, String internalName, IRMethod method, IRExpression expression) {
        if (expression == null)
            return;

        if (expression instanceof IRConstant constant) {
            emitConstant(mv, constant);
            return;
        }

        if (expression instanceof IRLocalLoad localLoad) {
            emitLocalLoad(mv, localLoad.local());
            return;
        }

        if (expression instanceof IRLocalStore localStore) {
            emitLocalStore(mv, internalName, method, localStore);
            return;
        }

        if (expression instanceof IRFieldLoad fieldLoad) {
            emitFieldLoad(mv, internalName, fieldLoad.field());
            return;
        }

        if (expression instanceof IRFieldStore fieldStore) {
            emitFieldStore(mv, internalName, method, fieldStore);
            return;
        }

        if (expression instanceof IRUnaryOperation unary) {
            emitUnaryOperation(mv, internalName, method, unary);
            return;
        }

        if (expression instanceof IRBinaryOperation binary) {
            emitBinaryOperation(mv, internalName, method, binary);
            return;
        }

        if (expression instanceof IRArrayLiteral arrayLiteral) {
            emitArrayLiteral(mv, internalName, method, arrayLiteral);
            return;
        }

        if (expression instanceof IRArrayConcat arrayConcat) {
            emitArrayConcat(mv, internalName, method, arrayConcat);
            return;
        }

        if (expression instanceof IRArrayGet arrayGet) {
            emitArrayGet(mv, internalName, method, arrayGet);
            return;
        }

        if (expression instanceof IRArraySet arraySet) {
            emitArraySet(mv, internalName, method, arraySet);
            return;
        }

        if (expression instanceof IRMappingLiteral mappingLiteral) {
            emitMappingLiteral(mv, internalName, method, mappingLiteral);
            return;
        }

        if (expression instanceof IRMappingMerge mappingMerge) {
            emitMappingMerge(mv, internalName, method, mappingMerge);
            return;
        }

        if (expression instanceof IRMappingGet mappingGet) {
            emitMappingGet(mv, internalName, method, mappingGet);
            return;
        }

        if (expression instanceof IRMappingSet mappingSet) {
            emitMappingSet(mv, internalName, method, mappingSet);
            return;
        }

        if (expression instanceof IREfunCall efunCall) {
            emitEfunCall(mv, internalName, method, efunCall);
            return;
        }

        if (expression instanceof IRInstanceCall instanceCall) {
            emitInstanceCall(mv, internalName, method, instanceCall);
            return;
        }

        if (expression instanceof IRDynamicInvoke dynamicInvoke) {
            emitDynamicInvoke(mv, internalName, method, dynamicInvoke);
            return;
        }

        if (expression instanceof IRCoerce coerce) {
            emitCoerce(mv, internalName, method, coerce);
        }
    }

    private void emitConstant(MethodVisitor mv, IRConstant constant) {
        Object value = constant.value();
        RuntimeType type = constant.type();

        if (value == null) {
            mv.visitInsn(ACONST_NULL);
            return;
        }

        switch (type.kind()) {
        case INT, STATUS -> pushInt(mv, ((Number) value).intValue());
        case FLOAT -> mv.visitLdcInsn(((Number) value).floatValue());
        case STRING -> mv.visitLdcInsn(value);
        default -> mv.visitLdcInsn(value);
        }
    }

    private void emitLocalLoad(MethodVisitor mv, IRLocal local) {
        switch (kindToOpcode(local.type(), true)) {
        case ILOAD -> mv.visitVarInsn(ILOAD, local.slot());
        case FLOAD -> mv.visitVarInsn(FLOAD, local.slot());
        default -> mv.visitVarInsn(ALOAD, local.slot());
        }
    }

    private void emitLocalStore(MethodVisitor mv, String internalName, IRMethod method, IRLocalStore localStore) {
        emitExpression(mv, internalName, method, localStore.value());

        RuntimeType type = localStore.local().type();
        dupForStore(mv, type);

        switch (kindToOpcode(type, false)) {
        case ISTORE -> mv.visitVarInsn(ISTORE, localStore.local().slot());
        case FSTORE -> mv.visitVarInsn(FSTORE, localStore.local().slot());
        default -> mv.visitVarInsn(ASTORE, localStore.local().slot());
        }
    }

    private void emitFieldLoad(MethodVisitor mv, String internalName, IRField field) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, internalName, field.name(), descriptor(field.type()));
    }

    private void emitFieldStore(MethodVisitor mv, String internalName, IRMethod method, IRFieldStore fieldStore) {
        mv.visitVarInsn(ALOAD, 0);
        emitExpression(mv, internalName, method, fieldStore.value());
        mv.visitInsn(DUP_X1);
        mv.visitFieldInsn(PUTFIELD, internalName, fieldStore.field().name(), descriptor(fieldStore.field().type()));
    }

    private void emitUnaryOperation(MethodVisitor mv, String internalName, IRMethod method, IRUnaryOperation unary) {
        emitExpression(mv, internalName, method, unary.operand());

        if (unary.operator() == UnaryOpType.UOP_NOT) {
            emitBooleanFlip(mv, unary.operand().type());
            return;
        }

        switch (unary.operator()) {
        case UOP_NEGATE:
            switch (unary.type().kind()) {
            case FLOAT -> mv.visitInsn(FNEG);
            default -> mv.visitInsn(INEG);
            }
            break;
        default:
            throw new UnsupportedOperationException("Unsupported unary operator: " + unary.operator());
        }
    }

    private void emitBinaryOperation(
            MethodVisitor mv, String internalName, IRMethod method, IRBinaryOperation binary) {
        BinaryOpType op = binary.operator();

        if (op == BinaryOpType.BOP_AND || op == BinaryOpType.BOP_OR) {
            emitLogicalBinary(mv, internalName, method, binary);
            return;
        }

        if (op == BinaryOpType.BOP_ADD && binary.type().kind() == RuntimeValueKind.STRING) {
            emitStringConcat(mv, internalName, method, binary.left(), binary.right());
            return;
        }

        emitIntOperand(mv, internalName, method, binary.left());
        emitIntOperand(mv, internalName, method, binary.right());

        switch (op) {
        case BOP_ADD -> mv.visitInsn(IADD);
        case BOP_SUB -> mv.visitInsn(ISUB);
        case BOP_MULT -> mv.visitInsn(IMUL);
        case BOP_DIV -> mv.visitInsn(IDIV);
        case BOP_GT, BOP_GE, BOP_LT, BOP_LE, BOP_EQ, BOP_NE -> emitComparison(mv, op);
        default -> throw new UnsupportedOperationException("Unsupported operator: " + op);
        }
    }

    private void emitLogicalBinary(
            MethodVisitor mv, String internalName, IRMethod method, IRBinaryOperation binary) {
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        Label end = new Label();

        emitBooleanValue(mv, internalName, method, binary.left());
        if (binary.operator() == BinaryOpType.BOP_OR)
            mv.visitJumpInsn(IFNE, trueLabel);
        else
            mv.visitJumpInsn(IFEQ, falseLabel);

        emitBooleanValue(mv, internalName, method, binary.right());
        if (binary.operator() == BinaryOpType.BOP_OR)
            mv.visitJumpInsn(IFNE, trueLabel);
        else
            mv.visitJumpInsn(IFEQ, falseLabel);

        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(GOTO, end);

        mv.visitLabel(falseLabel);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(end);
    }

    private void emitComparison(MethodVisitor mv, BinaryOpType op) {
        Label trueLabel = new Label();
        Label endLabel = new Label();

        mv.visitJumpInsn(op.opcode(), trueLabel);
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
    }

    private void emitIntOperand(MethodVisitor mv, String internalName, IRMethod method, IRExpression operand) {
        emitExpression(mv, internalName, method, operand);
        coerceValue(mv, operand.type(), RuntimeTypes.INT);
    }

    private void emitEfunCall(MethodVisitor mv, String internalName, IRMethod method, IREfunCall efunCall) {
        mv.visitMethodInsn(
                INVOKESTATIC,
                "io/github/protasm/lpc2j/runtime/RuntimeContextHolder",
                "requireCurrent",
                "()Lio/github/protasm/lpc2j/runtime/RuntimeContext;",
                false);
        mv.visitLdcInsn(efunCall.name());
        pushInt(mv, efunCall.arguments().size());
        emitArgumentsArray(mv, internalName, method, efunCall.arguments());

        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "io/github/protasm/lpc2j/runtime/RuntimeContext",
                "invokeEfun",
                "(Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/Object;",
                false);

        if (efunCall.type() != null && efunCall.type().kind() == RuntimeValueKind.VOID) {
            mv.visitInsn(POP);
            return;
        }

        emitCoerceToRuntimeTypeIfNeeded(mv, RuntimeTypes.MIXED, efunCall.type());
    }

    private void emitInstanceCall(MethodVisitor mv, String internalName, IRMethod method, IRInstanceCall call) {
        mv.visitVarInsn(ALOAD, 0);
        for (IRExpression arg : call.arguments())
            emitExpression(mv, internalName, method, arg);

        String descriptor = buildCallDescriptor(call);
        mv.visitMethodInsn(INVOKEVIRTUAL, call.ownerInternalName(), call.methodName(), descriptor, false);
    }

    private void emitDynamicInvoke(
            MethodVisitor mv, String internalName, IRMethod method, IRDynamicInvoke dynamicInvoke) {
        emitLocalLoad(mv, dynamicInvoke.targetLocal());
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitLdcInsn(dynamicInvoke.methodName());
        emitParamTypesArray(mv, internalName, method, dynamicInvoke.arguments());
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/Class",
                "getMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;",
                false);
        mv.visitInsn(SWAP);
        emitArgumentsArray(mv, internalName, method, dynamicInvoke.arguments());
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/reflect/Method",
                "invoke",
                "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                false);
        if (dynamicInvoke.type() != null && dynamicInvoke.type().kind() == RuntimeValueKind.VOID) {
            mv.visitInsn(POP);
            return;
        }
        emitCoerceToRuntimeTypeIfNeeded(mv, RuntimeTypes.MIXED, dynamicInvoke.type());
    }

    private void emitCoerce(MethodVisitor mv, String internalName, IRMethod method, IRCoerce coerce) {
        emitExpression(mv, internalName, method, coerce.value());
        coerceValue(mv, coerce.value().type(), coerce.targetType());
    }

    private void emitArgumentsArray(MethodVisitor mv, String internalName, IRMethod method, List<IRExpression> args) {
        pushInt(mv, args.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        for (int i = 0; i < args.size(); i++) {
            mv.visitInsn(DUP);
            pushInt(mv, i);
            IRExpression arg = args.get(i);
            emitExpression(mv, internalName, method, arg);
            boxIfNeeded(mv, arg.type());
            mv.visitInsn(AASTORE);
        }
    }

    private void emitParamTypesArray(
            MethodVisitor mv, String internalName, IRMethod method, List<IRExpression> args) {
        pushInt(mv, args.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

        for (int i = 0; i < args.size(); i++) {
            mv.visitInsn(DUP);
            pushInt(mv, i);
            RuntimeType type = args.get(i).type();
            switch (type != null ? type.kind() : RuntimeValueKind.MIXED) {
            case INT, STATUS -> mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            case FLOAT -> mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
            case STRING -> mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            default -> mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
            }
            mv.visitInsn(AASTORE);
        }
    }

    private void emitBooleanValue(MethodVisitor mv, String internalName, IRMethod method, IRExpression condition) {
        emitExpression(mv, internalName, method, condition);
        RuntimeType type = condition.type();

        if (type != null && type.kind() == RuntimeValueKind.STATUS && type.jvmType() != null) {
            return; // already boolean/int
        }

        boxIfNeeded(mv, type);
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Truth.class),
                "isTruthy",
                "(Ljava/lang/Object;)Z",
                false);
    }

    private void emitBooleanFlip(MethodVisitor mv, RuntimeType operandType) {
        boxIfNeeded(mv, operandType);
        mv.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(Truth.class),
                "isTruthy",
                "(Ljava/lang/Object;)Z",
                false);

        Label trueLabel = new Label();
        Label endLabel = new Label();
        mv.visitJumpInsn(IFEQ, trueLabel);
        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
    }

    private void emitStringConcat(
            MethodVisitor mv, String internalName, IRMethod method, IRExpression left, IRExpression right) {
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

        emitExpression(mv, internalName, method, left);
        boxIfNeeded(mv, left.type());
        mv.visitMethodInsn(
                INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);

        emitExpression(mv, internalName, method, right);
        boxIfNeeded(mv, right.type());
        mv.visitMethodInsn(
                INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);

        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    }

    private void emitCoerceToRuntimeTypeIfNeeded(MethodVisitor mv, RuntimeType source, RuntimeType target) {
        if (target == null)
            return;

        coerceValue(mv, source, target);
    }

    private void emitArrayLiteral(MethodVisitor mv, String internalName, IRMethod method, IRArrayLiteral literal) {
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);

        for (IRExpression element : literal.elements()) {
            mv.visitInsn(DUP);
            emitExpression(mv, internalName, method, element);
            boxIfNeeded(mv, element.type());
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
            mv.visitInsn(POP);
        }
    }

    private void emitArrayConcat(MethodVisitor mv, String internalName, IRMethod method, IRArrayConcat concat) {
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);

        mv.visitInsn(DUP);
        emitExpression(mv, internalName, method, concat.left());
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "addAll", "(Ljava/util/Collection;)Z", false);
        mv.visitInsn(POP);

        mv.visitInsn(DUP);
        emitExpression(mv, internalName, method, concat.right());
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "addAll", "(Ljava/util/Collection;)Z", false);
        mv.visitInsn(POP);
    }

    private void emitArrayGet(MethodVisitor mv, String internalName, IRMethod method, IRArrayGet arrayGet) {
        emitExpression(mv, internalName, method, arrayGet.array());
        mv.visitTypeInsn(CHECKCAST, "java/util/List");
        emitExpression(mv, internalName, method, arrayGet.index());
        coerceValue(mv, arrayGet.index().type(), RuntimeTypes.INT);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
    }

    private void emitArraySet(MethodVisitor mv, String internalName, IRMethod method, IRArraySet arraySet) {
        emitExpression(mv, internalName, method, arraySet.array());
        mv.visitTypeInsn(CHECKCAST, "java/util/List");
        emitExpression(mv, internalName, method, arraySet.index());
        coerceValue(mv, arraySet.index().type(), RuntimeTypes.INT);
        emitExpression(mv, internalName, method, arraySet.value());
        boxIfNeeded(mv, arraySet.value().type());
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", true);
    }

    private void emitMappingLiteral(MethodVisitor mv, String internalName, IRMethod method, IRMappingLiteral literal) {
        mv.visitTypeInsn(NEW, "java/util/HashMap");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);

        for (IRMappingEntry entry : literal.entries()) {
            mv.visitInsn(DUP);
            emitExpression(mv, internalName, method, entry.key());
            coerceValue(mv, entry.key().type(), RuntimeTypes.STRING);
            emitExpression(mv, internalName, method, entry.value());
            boxIfNeeded(mv, entry.value().type());
            mv.visitMethodInsn(
                    INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
            mv.visitInsn(POP);
        }
    }

    private void emitMappingMerge(MethodVisitor mv, String internalName, IRMethod method, IRMappingMerge merge) {
        mv.visitTypeInsn(NEW, "java/util/HashMap");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);

        mv.visitInsn(DUP);
        emitExpression(mv, internalName, method, merge.left());
        mv.visitTypeInsn(CHECKCAST, "java/util/Map");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "putAll", "(Ljava/util/Map;)V", true);

        mv.visitInsn(DUP);
        emitExpression(mv, internalName, method, merge.right());
        mv.visitTypeInsn(CHECKCAST, "java/util/Map");
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "putAll", "(Ljava/util/Map;)V", true);
    }

    private void emitMappingGet(MethodVisitor mv, String internalName, IRMethod method, IRMappingGet mappingGet) {
        emitExpression(mv, internalName, method, mappingGet.mapping());
        mv.visitTypeInsn(CHECKCAST, "java/util/Map");
        emitExpression(mv, internalName, method, mappingGet.key());
        coerceValue(mv, mappingGet.key().type(), RuntimeTypes.STRING);
        mv.visitMethodInsn(
                INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
    }

    private void emitMappingSet(MethodVisitor mv, String internalName, IRMethod method, IRMappingSet mappingSet) {
        emitExpression(mv, internalName, method, mappingSet.mapping());
        mv.visitTypeInsn(CHECKCAST, "java/util/Map");
        emitExpression(mv, internalName, method, mappingSet.key());
        coerceValue(mv, mappingSet.key().type(), RuntimeTypes.STRING);
        emitExpression(mv, internalName, method, mappingSet.value());
        boxIfNeeded(mv, mappingSet.value().type());
        mv.visitMethodInsn(
                INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
    }

    private void coerceValue(MethodVisitor mv, RuntimeType source, RuntimeType target) {
        if (target == null || target.kind() == RuntimeValueKind.VOID)
            return;

        if (source != null && target.equals(source))
            return;

        switch (target.kind()) {
        case STATUS:
            boxIfNeeded(mv, source);
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(Truth.class),
                    "isTruthy",
                    "(Ljava/lang/Object;)Z",
                    false);
            return;
        case INT:
            if (source != null && source.kind() == RuntimeValueKind.FLOAT) {
                mv.visitInsn(F2I);
                return;
            }
            if (source != null && !source.isReferenceLike())
                return;

            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
            return;
        case FLOAT:
            if (source != null && source.kind() == RuntimeValueKind.INT) {
                mv.visitInsn(I2F);
                return;
            }
            if (source != null && !source.isReferenceLike())
                return;

            mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "floatValue", "()F", false);
            return;
        default:
            if (source != null && !source.isReferenceLike())
                boxIfNeeded(mv, source);

            if (target.objectInternalName() != null)
                mv.visitTypeInsn(CHECKCAST, target.objectInternalName());
        }
    }

    private void boxIfNeeded(MethodVisitor mv, RuntimeType type) {
        if (type == null)
            return;

        switch (type.kind()) {
        case INT, STATUS -> mv.visitMethodInsn(
                INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        case FLOAT -> mv.visitMethodInsn(
                INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        default -> {}
        }
    }

    private void dupForStore(MethodVisitor mv, RuntimeType type) {
        if (type == null)
            mv.visitInsn(DUP);
        else
            mv.visitInsn(DUP);
    }

    private int kindToOpcode(RuntimeType type, boolean load) {
        if (type == null)
            return load ? ALOAD : ASTORE;

        return switch (type.kind()) {
        case INT, STATUS -> load ? ILOAD : ISTORE;
        case FLOAT -> load ? FLOAD : FSTORE;
        default -> load ? ALOAD : ASTORE;
        };
    }

    private String descriptor(RuntimeType type) {
        return (type != null) ? type.descriptor() : "Ljava/lang/Object;";
    }

    private String methodDescriptor(IRMethod method) {
        StringBuilder sb = new StringBuilder("(");
        for (IRParameter parameter : method.parameters())
            sb.append(descriptor(parameter.type()));

        sb.append(")");
        sb.append(descriptor(method.returnType()));
        return sb.toString();
    }

    private String buildCallDescriptor(IRInstanceCall call) {
        if (call.parameterTypes() != null && !call.parameterTypes().isEmpty())
            return buildCallDescriptor(call.parameterTypes(), call.type());

        return buildCallDescriptorFromArgs(call.arguments(), call.type());
    }

    private String buildCallDescriptor(List<RuntimeType> parameterTypes, RuntimeType returnType) {
        StringBuilder sb = new StringBuilder("(");
        for (RuntimeType parameterType : parameterTypes)
            sb.append(descriptor(parameterType));
        sb.append(")");
        sb.append(descriptor(returnType));
        return sb.toString();
    }

    private String buildCallDescriptorFromArgs(List<IRExpression> args, RuntimeType returnType) {
        StringBuilder sb = new StringBuilder("(");
        for (IRExpression arg : args)
            sb.append(descriptor(arg.type()));
        sb.append(")");
        sb.append(descriptor(returnType));
        return sb.toString();
    }

    private void pushInt(MethodVisitor mv, int value) {
        if ((value >= -1) && (value <= 5))
            mv.visitInsn(ICONST_0 + value);
        else if ((value >= Byte.MIN_VALUE) && (value <= Byte.MAX_VALUE))
            mv.visitIntInsn(BIPUSH, value);
        else if ((value >= Short.MIN_VALUE) && (value <= Short.MAX_VALUE))
            mv.visitIntInsn(SIPUSH, value);
        else
            mv.visitLdcInsn(value);
    }
}
