package io.github.protasm.lpc2j.ir;

public sealed interface IRNode permits IRExpression, IRStatement {
    int line();
}
