package io.github.protasm.lpc2j.parser.type;

import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.ISUB;

public enum BinaryOpType {
    BOP_ADD(IADD), BOP_SUB(ISUB), BOP_MULT(IMUL), BOP_DIV(IDIV), BOP_GT(IF_ICMPGT), BOP_LT(IF_ICMPLT),
    BOP_EQ(IF_ICMPEQ), BOP_GE(IF_ICMPGE), BOP_LE(IF_ICMPLE), BOP_OR(-1), BOP_AND(-1);

    private final int opcode;

    BinaryOpType(int opcode) {
        this.opcode = opcode;
    }

    public int opcode() {
        return opcode;
    }

}
