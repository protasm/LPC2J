package io.github.protasm.lpc2j.compiler;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.FNEG;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INEG;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V23;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import io.github.protasm.lpc2j.efun.Efun;
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
import io.github.protasm.lpc2j.parser.ast.ASTExpression;
import io.github.protasm.lpc2j.parser.ast.ASTStatement;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtBlock;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtExpression;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtIfThenElse;
import io.github.protasm.lpc2j.parser.ast.stmt.ASTStmtReturn;
import io.github.protasm.lpc2j.parser.ast.visitor.ASTVisitor;
import io.github.protasm.lpc2j.parser.type.BinaryOpType;
import io.github.protasm.lpc2j.parser.type.JType;
import io.github.protasm.lpc2j.parser.type.LPCType;
import io.github.protasm.lpc2j.parser.type.UnaryOpType;
import io.github.protasm.lpc2j.runtime.Truth;

public class Compiler implements ASTVisitor {
        private final String defaultParentName;
        private final ClassWriter cw;
        private MethodVisitor mv; // current method
        private LPCType currentReturnType;

    public Compiler(String defaultParentName) {
        this.defaultParentName = defaultParentName;

        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    }

    public byte[] compile(ASTObject astObject) {
        if (astObject == null)
            throw new CompileException("ASTObject cannot be null.");

        try {
            astObject.accept(this);

            return this.cw.toByteArray();
        } catch (CompileException e) {
            throw e;
        } catch (RuntimeException e) {
            String name = (astObject.name() != null) ? astObject.name() : "<unnamed>";

            throw new CompileException("Failed to compile object '" + name + "': " + e.getMessage(), e);
        }
    }

    @Override
    public void visitArgument(ASTArgument arg) {
        arg.expression().accept(this);

        LPCType type = arg.expression().lpcType();

        if (type == null) // Without type information, assume no boxing is needed.
            return;

        // box primitive value, if needed
        switch (type.jType()) {
        case JINT: // Integer.valueOf(int)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            break;
        case JFLOAT: // Float.valueOf(float)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            break;
        case JBOOLEAN: // Boolean.valueOf(boolean)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            break;
        default: // For non-primitive types (or types that don't need boxing).
            break;
        }
    }

    @Override
    public void visitArguments(ASTArguments args) {
        pushInt(args.size()); // Push array length

        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object"); // Create Object[]

        for (int i = 0; i < args.size(); i++) {
            mv.visitInsn(DUP); // Duplicate array reference

            pushInt(i); // Push index

            args.nodes().get(i).accept(this); // Push argument value

            mv.visitInsn(AASTORE); // Store argument into array
        }
    }

    @Override
    public void visitExprCallMethod(ASTExprCallMethod expr) {
        ASTMethod method = expr.method();
        ASTArguments args = expr.arguments();

        // Load "this" reference
        mv.visitVarInsn(Opcodes.ALOAD, 0);

        // Push each argument value individually on the stack. The invoked
        // method expects its arguments directly on the operand stack
        // according to its descriptor (e.g. an int for "(I)I").
        for (ASTArgument arg : args.nodes())
            arg.expression().accept(this);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, method.ownerName(), method.symbol().name(), method.descriptor(),
                false);

        // Pop if the method returns a value but it's unused
//        if (!method.lpcReturnType().equals("V"))
//            mv.visitInsn(Opcodes.POP);
    }

    @Override
    public void visitExprCallEfun(ASTExprCallEfun expr) {
        Efun efun = expr.efun();
        ASTArguments args = expr.arguments();

        mv.visitLdcInsn(efun.symbol().name());

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "io/github/protasm/lpc2j/efun/EfunRegistry", "lookup",
                "(Ljava/lang/String;)Lio/github/protasm/lpc2j/efun/Efun;", false);

        // Null-check to avoid null-pointer error
        var ok = new org.objectweb.asm.Label();

        mv.visitInsn(Opcodes.DUP);
        mv.visitJumpInsn(Opcodes.IFNONNULL, ok);
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn("Unknown efun: '" + efun.symbol().name() + "'");

        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V",
                false);

        mv.visitInsn(Opcodes.ATHROW);
        mv.visitLabel(ok);

        // For non-static invocation, bundle arguments in an Object[] array
        args.accept(this);

        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "io/github/protasm/lpc2j/efun/Efun", "invoke",
                "([Ljava/lang/Object;)Ljava/lang/Object;", true);
    }

    @Override
    public void visitExprFieldAccess(ASTExprFieldAccess expr) {
        ASTField field = expr.field();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, field.ownerName(), field.symbol().name(), field.descriptor());
    }

    @Override
    public void visitExprFieldStore(ASTExprFieldStore expr) {
        ASTField field = expr.field();
        ASTExpression value = expr.value();

        mv.visitVarInsn(ALOAD, 0);

        value.accept(this);

        coerceAssignmentValue(field.symbol().lpcType(), value);

        mv.visitInsn(Opcodes.DUP_X1);
        mv.visitFieldInsn(PUTFIELD, field.ownerName(), field.symbol().name(), field.symbol().descriptor());
    }

    @Override
    public void visitExprInvokeLocal(ASTExprInvokeLocal expr) {
        ASTArguments args = expr.args();

        // Load target object
        invokeLoadLocalObj(expr.slot());

        // Call getClass() on target object
        invokeGetClass();

        // Load method name
        mv.visitLdcInsn(expr.methodName());

        // Load method parameter types (Class[])
        invokeParamTypes(args);

        // Invoke Class.getMethod(String, Class[]) to get Method object
        invokeGetMethod();

        // Reload target object (for invoke call)
        invokeLoadLocalObj(expr.slot());

        // Load actual argument values (Object[])
        args.accept(this);

        // Invoke Method.invoke(Object, Object[]), returning Object
        invokeMethodInvoke();

        // Unbox/cast return value
        invokeReturnValue(expr.lpcType());
    }

    @Override
    public void visitExprLiteralFalse(ASTExprLiteralFalse expr) {
        mv.visitInsn(Opcodes.ICONST_0);
    }

    @Override
    public void visitExprLiteralInteger(ASTExprLiteralInteger expr) {
        Integer value = expr.value();

        pushInt(value);
    }

    @Override
    public void visitExprLiteralString(ASTExprLiteralString expr) {
        mv.visitLdcInsn(expr.value());
    }

    @Override
    public void visitExprLiteralTrue(ASTExprLiteralTrue expr) {
        mv.visitInsn(Opcodes.ICONST_1);
    }

    @Override
    public void visitExprLocalAccess(ASTExprLocalAccess expr) {
        ASTLocal local = expr.local();

        switch (local.symbol().lpcType()) {
        case LPCINT:
        case LPCSTATUS:
            mv.visitVarInsn(ILOAD, local.slot());
            break;
        case LPCSTRING:
        case LPCOBJECT:
        case LPCMIXED:
            mv.visitVarInsn(ALOAD, local.slot());
            break;
        default:
            throw new IllegalStateException("Unsupported type: " + local.symbol().lpcType());
        }
    }

    @Override
    public void visitExprLocalStore(ASTExprLocalStore expr) {
        ASTLocal local = expr.local();
        ASTExpression value = expr.value();

        value.accept(this);

        coerceAssignmentValue(local.symbol().lpcType(), value);

        switch (local.symbol().lpcType()) {
        case LPCINT:
        case LPCSTATUS:
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(ISTORE, local.slot());
            break;
        case LPCSTRING:
        case LPCOBJECT:
        case LPCMIXED:
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(ASTORE, local.slot());
            break;
        default:
            throw new IllegalStateException("Unsupported type: " + local.symbol().lpcType());
        }
    }

    @Override
    public void visitExprNull(ASTExprNull expr) {
        mv.visitInsn(ACONST_NULL);
    }

    @Override
    public void visitExprOpBinary(ASTExprOpBinary expr) {
        ASTExpression left = expr.left();
        ASTExpression right = expr.right();
        BinaryOpType operator = expr.operator();

        switch (operator) {
        case BOP_ADD:
            if (expr.lpcType() == LPCType.LPCSTRING)
                emitStringConcat(left, right);
            else {
                left.accept(this);
                right.accept(this);

                mv.visitInsn(operator.opcode());
            }
            break;
        case BOP_SUB:
        case BOP_MULT:
        case BOP_DIV:
            left.accept(this);
            right.accept(this);

            mv.visitInsn(operator.opcode());
            break;
        case BOP_GT:
        case BOP_GE:
        case BOP_LT:
        case BOP_LE:
        case BOP_EQ:
        case BOP_NE:
            left.accept(this);
            right.accept(this);

            Label labelTrue = new Label();
            Label labelEnd = new Label();

            // Compare left vs right
            mv.visitJumpInsn(operator.opcode(), labelTrue);

            // False case: Push 0 (false)
            mv.visitInsn(ICONST_0);
            mv.visitJumpInsn(GOTO, labelEnd);

            // True case: Push 1 (true)
            mv.visitLabel(labelTrue);
            mv.visitInsn(ICONST_1);

            // End label
            mv.visitLabel(labelEnd);
            break;
        case BOP_AND:
            left.accept(this);
            coerceToBoolean(left);

            Label falseLabel = new Label();
            Label endAndLabel = new Label();

            // Short-circuit if the left operand is false.
            mv.visitJumpInsn(IFEQ, falseLabel);

            right.accept(this);
            coerceToBoolean(right);
            mv.visitJumpInsn(IFEQ, falseLabel);

            mv.visitInsn(ICONST_1);
            mv.visitJumpInsn(GOTO, endAndLabel);

            mv.visitLabel(falseLabel);
            mv.visitInsn(ICONST_0);

            mv.visitLabel(endAndLabel);
            break;
        case BOP_OR:
            left.accept(this);
            coerceToBoolean(left);

            Label trueLabel = new Label();
            Label endOrLabel = new Label();

            // Short-circuit if the left operand is true.
            mv.visitJumpInsn(IFNE, trueLabel);

            right.accept(this);
            coerceToBoolean(right);
            mv.visitJumpInsn(IFNE, trueLabel);

            mv.visitInsn(ICONST_0);
            mv.visitJumpInsn(GOTO, endOrLabel);

            mv.visitLabel(trueLabel);
            mv.visitInsn(ICONST_1);

            mv.visitLabel(endOrLabel);
            break;
        default:
            throw new UnsupportedOperationException("Unsupported operator: " + operator);
        }
    }

    @Override
    public void visitExprOpUnary(ASTExprOpUnary expr) {
        ASTExpression right = expr.right();
        UnaryOpType operator = expr.operator();

        right.accept(this);

        if (operator == UnaryOpType.UOP_NOT)
            coerceToBoolean(right);

        switch (operator) {
        case UOP_NEGATE: // Unary minus (-)
            switch (coerceUnaryNumericOperand(right)) {
            case JFLOAT:
                mv.visitInsn(FNEG);
                break;
            default:
                mv.visitInsn(INEG);
                break;
            }
            break;
        case UOP_NOT: // Logical NOT (!)
            Label trueLabel = new Label();
            Label endLabel = new Label();

            // Jump if operand is false (0)
            mv.visitJumpInsn(IFEQ, trueLabel);

            // Operand is true, push 0 (false)
            mv.visitInsn(ICONST_0);
            mv.visitJumpInsn(GOTO, endLabel);

            // Operand is false, push 1 (true)
            mv.visitLabel(trueLabel);
            mv.visitInsn(ICONST_1);

            // End
            mv.visitLabel(endLabel);

            break;
        }
    }

    @Override
    public void visitField(ASTField field) {
        FieldVisitor fv = cw.visitField(ACC_PRIVATE, field.symbol().name(), field.descriptor(), null, null);

        // initializer bytecode deferred to constructor

        fv.visitEnd();
    }

    @Override
    public void visitFields(ASTFields fields) {
        for (ASTField field : fields)
            field.accept(this);
    }

    @Override
    public void visitLocal(ASTLocal local) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visitMethod(ASTMethod method) {
        currentReturnType = method.symbol().lpcType();

        method.body().accept(this);

        // LPC allows falling off the end of a function. Ensure a
        // default return value is emitted so the generated bytecode
        // always has a terminating return instruction that matches the
        // declared return type.
        emitDefaultReturn();
    }

    @Override
    public void visitMethods(ASTMethods methods) {
        for (ASTMethod method : methods) {
            mv = cw.visitMethod( // current method
                    ACC_PUBLIC, method.symbol().name(), method.descriptor(), null, null);

            mv.visitCode();

            method.accept(this);

            mv.visitMaxs(0, 0); // Automatically calculated by ASM
            mv.visitEnd();
        }
    }

    @Override
    public void visitObject(ASTObject object) {
        String parentName;

        if (object.parentName() != null)
            parentName = object.parentName();
        else
            parentName = defaultParentName;

        cw.visit(V23, ACC_SUPER | ACC_PUBLIC, object.name(), null, parentName, null);

        object.fields().accept(this);

        constructor(object, parentName); // initializers

        object.methods().accept(this);
    }

    @Override
    public void visitStmtBlock(ASTStmtBlock stmt) {
        for (ASTStatement statement : stmt)
            statement.accept(this);
    }

    @Override
    public void visitStmtExpression(ASTStmtExpression stmt) {
        stmt.expression().accept(this);

        // Expression statements should not leave stray values on the stack. Most
        // expressions produce a value (method calls, assignments, etc.), so drop
        // it unless the expression is explicitly void.
        var type = stmt.expression().lpcType();

        if (type == null || type != LPCType.LPCVOID)
            mv.visitInsn(Opcodes.POP);
    }

    @Override
    public void visitStmtIfThenElse(ASTStmtIfThenElse stmt) {
        Label elseLabel = new Label();
        Label endLabel = new Label();
        ASTExpression condition = stmt.condition();
        ASTStatement thenBranch = stmt.thenBranch();
        ASTStatement elseBranch = stmt.elseBranch();

        // Generate bytecode for condition
        condition.accept(this);
        coerceToBoolean(condition);

        // If condition is false, jump to else (or end if no else)
        mv.visitJumpInsn(IFEQ, elseBranch != null ? elseLabel : endLabel);

        // Generate bytecode for then-branch
        thenBranch.accept(this);

        // Skip else-branch (if it exists)
        if (elseBranch != null) {
            mv.visitJumpInsn(GOTO, endLabel);

            mv.visitLabel(elseLabel);

            elseBranch.accept(this);
        }

        // End label
        mv.visitLabel(endLabel);
    }

    @Override
    public void visitStmtReturn(ASTStmtReturn stmt) {
        ASTExpression returnValue = stmt.returnValue();

        if (returnValue == null) {
            emitDefaultReturn();
            return;
        }

        returnValue.accept(this);

        // Ensure the emitted return opcode matches the declared (or
        // inferred) method return type, not just the expression type
        // of this particular return statement. This keeps the JVM
        // verifier happy even when an LPC function is implicitly
        // "mixed" but returns a primitive value.
        coerceAssignmentValue(currentReturnType, returnValue);

        LPCType returnType = currentReturnType != null ? currentReturnType : returnValue.lpcType();

        switch (returnType) {
        case LPCINT:
        case LPCSTATUS:
            mv.visitInsn(Opcodes.IRETURN);
            break;
        case LPCNULL:
            mv.visitInsn(Opcodes.ARETURN);
            break;
        case LPCMIXED:
        case LPCSTRING:
        case LPCOBJECT:
            mv.visitInsn(Opcodes.ARETURN);
            break;
        default:
            throw new UnsupportedOperationException("Unsupported return value type: " + returnValue.lpcType());
        }
    }

    private void emitDefaultReturn() {
        switch (currentReturnType) {
        case LPCINT:
        case LPCSTATUS:
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitInsn(Opcodes.IRETURN);
            break;
        case LPCSTRING:
        case LPCOBJECT:
        case LPCMIXED:
        case LPCNULL:
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
            break;
        case LPCVOID:
            mv.visitInsn(Opcodes.RETURN);
            break;
        default:
            throw new UnsupportedOperationException("Unsupported implicit return type: " + currentReturnType);
        }
    }

    private void constructor(ASTObject object, String parentName) {
        mv = cw.visitMethod( // current method
                ACC_PUBLIC, "<init>", "()V", null, null);

        mv.visitCode();

        // Call super constructor
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, parentName, "<init>", "()V", false);

        // Initialize fields
        for (ASTField field : object.fields())
            if (field.initializer() != null) {
                mv.visitVarInsn(ALOAD, 0); // Load 'this'

                field.initializer().accept(this);

                coerceAssignmentValue(field.symbol().lpcType(), field.initializer());

                mv.visitFieldInsn(PUTFIELD, object.name(), field.symbol().name(), field.descriptor());
            }

        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0); // Automatically calculated by ASM
        mv.visitEnd();
    }

//    public byte[] bytes() {
//    return cw.toByteArray();
//    }

    private void invokeParamTypes(ASTArguments args) {
        pushInt(args.size());

        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

        for (int i = 0; i < args.size(); i++) {
            mv.visitInsn(DUP); // Duplicate array reference.

            pushInt(i); // Push array index.

            // Get the LPC type for the i-th argument.
            ASTExpression expr = args.nodes().get(i).expression();
            JType jType = expr.lpcType().jType();

            switch (jType) {
            case JINT:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                break;
            case JFLOAT:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                break;
            case JBOOLEAN:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                break;
            case JSTRING:
                mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
                break;
            default:
                // For LPCMIXED or other types, default to Object.
                mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
                break;
            }

            mv.visitInsn(AASTORE);
        }
    }

    private void invokeLoadLocalObj(int slot) {
        mv.visitVarInsn(ALOAD, slot);
    }

    private void invokeGetClass() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
    }

    private void invokeGetMethod() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod",
                "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
    }

    private void invokeMethodInvoke() {
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
                "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
    }

    private void invokeReturnValue(LPCType lpcType) {
        if (lpcType != null)
            switch (lpcType.jType()) {
            case JINT:
                // Cast to Integer and unbox to int.
                mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                break;
            case JFLOAT:
                // Cast to Float and unbox to float.
                mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                break;
            case JBOOLEAN:
                // Cast to Boolean and unbox to boolean.
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                break;
            case JSTRING:
                // Cast to String.
                mv.visitTypeInsn(CHECKCAST, "java/lang/String");
                break;
            default:
                // For LPCMIXED or other types, leave the result as Object,
                // or add an appropriate cast if necessary.
                break;
            }
    }

    private void coerceToBoolean(ASTExpression expr) {
        LPCType type = expr.lpcType();

        if (type != null)
            switch (type.jType()) {
            case JBOOLEAN:
            case JINT:
                return; // already an int-compatible boolean
            case JFLOAT:
                // Box float so truthiness can be evaluated uniformly.
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                break;
            default:
                break; // other types handled below
            }

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Truth.class), "isTruthy",
                "(Ljava/lang/Object;)Z", false);
    }

    private void coerceAssignmentValue(LPCType targetType, ASTExpression value) {
        if (targetType == null || value == null)
            return;

        switch (targetType.jType()) {
        case JSTRING:
            if (isLiteralZero(value)) {
                mv.visitInsn(Opcodes.POP);
                mv.visitInsn(Opcodes.ACONST_NULL);
            }
            break;
        case JOBJECT:
            if (targetType == LPCType.LPCMIXED)
                boxPrimitiveIfNeeded(value.lpcType());
            else if (isLiteralZero(value)) {
                mv.visitInsn(Opcodes.POP);
                mv.visitInsn(Opcodes.ACONST_NULL);
            }
            break;
        default:
            break;
        }
    }

    private JType coerceUnaryNumericOperand(ASTExpression operand) {
        LPCType type = operand.lpcType();

        if (type != null)
            switch (type.jType()) {
            case JINT:
            case JBOOLEAN:
                return JType.JINT;
            case JFLOAT:
                return JType.JFLOAT;
            case JOBJECT:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
                return JType.JINT;
            default:
                break;
            }

        mv.visitTypeInsn(CHECKCAST, "java/lang/Number");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Number", "intValue", "()I", false);
        return JType.JINT;
    }

    private boolean isLiteralZero(ASTExpression expr) {
        return expr instanceof ASTExprLiteralInteger && ((ASTExprLiteralInteger) expr).value() == 0;
    }

    private void boxPrimitiveIfNeeded(LPCType sourceType) {
        if (sourceType == null)
            return;

        switch (sourceType.jType()) {
        case JINT:
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            break;
        case JFLOAT:
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            break;
        case JBOOLEAN:
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            break;
        default:
            break;
        }
    }

    private void emitStringConcat(ASTExpression left, ASTExpression right) {
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

        left.accept(this);
        appendStringBuilder(left.lpcType());

        right.accept(this);
        appendStringBuilder(right.lpcType());

        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
    }

    private void appendStringBuilder(LPCType type) {
        String descriptor;

        if (type != null && type.jType() != null)
            switch (type.jType()) {
            case JINT:
                descriptor = "(I)Ljava/lang/StringBuilder;";
                break;
            case JFLOAT:
                descriptor = "(F)Ljava/lang/StringBuilder;";
                break;
            case JBOOLEAN:
                descriptor = "(Z)Ljava/lang/StringBuilder;";
                break;
            case JSTRING:
                descriptor = "(Ljava/lang/String;)Ljava/lang/StringBuilder;";
                break;
            default:
                descriptor = "(Ljava/lang/Object;)Ljava/lang/StringBuilder;";
                break;
            }
        else
            descriptor = "(Ljava/lang/Object;)Ljava/lang/StringBuilder;";

        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", descriptor, false);
    }

    private void pushInt(int value) {
        if ((value >= -1) && (value <= 5))
            mv.visitInsn(Opcodes.ICONST_0 + value);
        else if ((value >= Byte.MIN_VALUE) && (value <= Byte.MAX_VALUE))
            mv.visitIntInsn(Opcodes.BIPUSH, value);
        else if ((value >= Short.MIN_VALUE) && (value <= Short.MAX_VALUE))
            mv.visitIntInsn(Opcodes.SIPUSH, value);
        else
            mv.visitLdcInsn(value);
    }

    @Override
    public void visitParameter(ASTParameter param) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visitParameters(ASTParameters params) {
        // TODO Auto-generated method stub
    }
}
