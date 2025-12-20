package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;

public sealed interface IRNode permits IRExpression, IRStatement {
    int line();
}

public sealed interface IRExpression extends IRNode
        permits IRBinaryOperation,
                IRCoerce,
                IRConstant,
                IRDynamicInvoke,
                IREfunCall,
                IRFieldLoad,
                IRFieldStore,
                IRInstanceCall,
                IRLocalLoad,
                IRLocalStore,
                IRUnaryOperation {
    RuntimeType type();
}

public sealed interface IRStatement extends IRNode permits IRConditionalJump, IRExpressionStatement, IRJump, IRReturn {}

public sealed interface IRTerminator extends IRStatement permits IRConditionalJump, IRJump, IRReturn {}
