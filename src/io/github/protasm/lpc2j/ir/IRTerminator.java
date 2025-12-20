package io.github.protasm.lpc2j.ir;

public sealed interface IRTerminator extends IRStatement permits IRConditionalJump, IRJump, IRReturn {}
