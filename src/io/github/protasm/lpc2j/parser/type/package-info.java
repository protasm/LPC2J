/**
 * Type metadata used by the parser and compiler.
 *
 * <p>Models LPC-visible types ({@link io.github.protasm.lpc2j.parser.type.LPCType}) alongside helper
 * enums describing Java-level representations ({@link io.github.protasm.lpc2j.parser.type.JType}) and
 * operator classifications ({@link io.github.protasm.lpc2j.parser.type.BinaryOpType},
 * {@link io.github.protasm.lpc2j.parser.type.UnaryOpType}, {@link
 * io.github.protasm.lpc2j.parser.type.InstrType}).</p>
 *
 * <p>Supports basic conversions between LPC and JVM descriptors used during compilation and type
 * inference. This package encodes expectations about operator result types but does not emit bytecode
 * or perform runtime checks.</p>
 */
package io.github.protasm.lpc2j.parser.type;
