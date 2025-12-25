package io.github.protasm.lpc2j.ir;

import io.github.protasm.lpc2j.runtime.RuntimeType;

public sealed interface IRExpression extends IRNode
        permits IRArrayConcat,
                IRArrayGet,
                IRArrayLiteral,
                IRArraySet,
                IRBinaryOperation,
                IRCoerce,
                IRConditionalExpression,
                IRConstant,
                IRDynamicInvoke,
                IREfunCall,
                IRFieldLoad,
                IRFieldStore,
                IRInstanceCall,
                IRLocalLoad,
                IRLocalStore,
                IRMappingGet,
                IRMappingLiteral,
                IRMappingMerge,
                IRMappingSet,
                IRUnaryOperation {
    RuntimeType type();
}
