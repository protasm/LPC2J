package io.github.protasm.lpc2j.ir;

public sealed interface IRStatement extends IRNode permits IRExpressionStatement, IRTerminator {}
