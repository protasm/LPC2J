package io.github.protasm.lpc2j;

import java.util.HashMap;
import java.util.Map;

public final class J_OpCode {
  public static final byte OP_ACONST_NULL   = (byte) 0x01; //push null on stack
  public static final byte OP_ICONST_0      = (byte) 0x03; //push 0 on stack
  public static final byte OP_ICONST_1      = (byte) 0x04; //push 1 on stack
  public static final byte OP_SIPUSH        = (byte) 0x11; //push short on stack
  public static final byte OP_LDC           = (byte) 0x12; //load constant
  public static final byte OP_LDC_W         = (byte) 0x13; //load constant (wide idx)
  public static final byte OP_LDC2_W        = (byte) 0x14; //load constant (wide idx, +2 stack)
  public static final byte OP_ALOAD_0       = (byte) 0x2a; //load Local #0 on stack
  public static final byte OP_ALOAD_1       = (byte) 0x2b; //load Local #1 on stack
  public static final byte OP_ALOAD_2       = (byte) 0x2c; //load Local #2 on stack
  public static final byte OP_ALOAD_3       = (byte) 0x2d; //load Local #3 on stack
  public static final byte OP_POP           = (byte) 0x57; //pop top stack value
  public static final byte OP_ISTORE_0      = (byte) 0x3b; //store stacked int in Local #0
  public static final byte OP_ISTORE_1      = (byte) 0x3c; //store stacked int in Local #1
  public static final byte OP_ISTORE_2      = (byte) 0x3d; //store stacked int in Local #2
  public static final byte OP_ISTORE_3      = (byte) 0x3e; //store stacked int in Local #3
  public static final byte OP_SWAP          = (byte) 0x5f; //swap top two stack values
  public static final byte OP_INEG          = (byte) 0x74; //negate integer
  public static final byte OP_FNEG          = (byte) 0x76; //negate float
  public static final byte OP_IRETURN       = (byte) 0xac; //return integer
  public static final byte OP_FRETURN       = (byte) 0xae; //return float
  public static final byte OP_ARETURN       = (byte) 0xb0; //return object reference
  public static final byte OP_RETURN        = (byte) 0xb1; //return void from method
  public static final byte OP_GETFIELD      = (byte) 0xb4; //load value of objectref.fieldref
  public static final byte OP_PUTFIELD      = (byte) 0xb5; //set objectref.fieldref to value
  public static final byte OP_INVOKESPECIAL = (byte) 0xb7; //invoke methodref on objectref (stack)

  private J_OpCode() {}

  private static final Map<Byte, Integer> stackIncMap = new HashMap<>();
  private static final Map<Byte, Integer> localsRefMap = new HashMap<>();

  static {
    stackIncMap.put(OP_ACONST_NULL,    1);
    stackIncMap.put(OP_ICONST_0,       1);
    stackIncMap.put(OP_ICONST_1,       1);
    stackIncMap.put(OP_SIPUSH,         1);
    stackIncMap.put(OP_LDC,            1);
    stackIncMap.put(OP_LDC_W,          1);
    stackIncMap.put(OP_LDC2_W,         2);
    stackIncMap.put(OP_ALOAD_0,        1);
    stackIncMap.put(OP_ALOAD_1,        1);
    stackIncMap.put(OP_ALOAD_2,        1);
    stackIncMap.put(OP_ALOAD_3,        1);
    stackIncMap.put(OP_POP,           -1);
    stackIncMap.put(OP_ISTORE_0,      -1);
    stackIncMap.put(OP_ISTORE_1,      -1);
    stackIncMap.put(OP_ISTORE_2,      -1);
    stackIncMap.put(OP_ISTORE_3,      -1);
    stackIncMap.put(OP_IRETURN,       -1);
    stackIncMap.put(OP_FRETURN,       -1);
    stackIncMap.put(OP_ARETURN,       -1);
    stackIncMap.put(OP_PUTFIELD,      -2);

    localsRefMap.put(OP_ALOAD_1,       1);
    localsRefMap.put(OP_ALOAD_2,       2);
    localsRefMap.put(OP_ALOAD_3,       3);
    localsRefMap.put(OP_ISTORE_1,      1);
    localsRefMap.put(OP_ISTORE_2,      2);
    localsRefMap.put(OP_ISTORE_3,      3);
  }

  //stackInc(byte)
  public static int stackInc(byte opCode) {
	if (!stackIncMap.containsKey(opCode))
	  return 0;

    return stackIncMap.get(opCode);
  } //stackInc(byte)

  //localsRef(byte)
  public static int localsRef(byte opCode) {
	if (!localsRefMap.containsKey(opCode))
      return 0;

    return localsRefMap.get(opCode);
  } //localsRef(byte)
} //J_OpCode