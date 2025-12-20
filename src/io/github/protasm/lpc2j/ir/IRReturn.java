package io.github.protasm.lpc2j.ir;

public record IRReturn(int line, IRExpression returnValue) implements IRTerminator {}
