package io.github.protasm.lpc2j.parser;

import static org.objectweb.asm.Opcodes.*;

public enum BinaryOpType {
    BOP_ADD(IADD), BOP_SUB(ISUB), BOP_MULT(IMUL), BOP_DIV(IDIV), BOP_GT(IF_ICMPGT), BOP_LT(IF_ICMPLT),
    BOP_EQ(IF_ICMPEQ), BOP_GE(IF_ICMPGE), BOP_LE(IF_ICMPLE);

    private final int opcode;

    BinaryOpType(int opcode) {
	this.opcode = opcode;
    }

    public int opcode() {
	return opcode;
    }

}
