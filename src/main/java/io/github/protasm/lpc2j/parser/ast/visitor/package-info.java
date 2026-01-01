/**
 * Visitor implementations that operate over the LPC AST.
 *
 * <p>Includes utilities for printing the tree and inferring {@link
 * io.github.protasm.lpc2j.parser.type.LPCType} information before compilation. Visitors are expected
 * to be run in a defined order (e.g., type inference after parsing) to ensure downstream consumers
 * like the compiler see fully annotated nodes.</p>
 *
 * <p>Visitors are not responsible for mutating program structure beyond attaching type data or debug
 * output; semantic enforcement beyond these passes belongs to parser or compiler stages.</p>
 */
package io.github.protasm.lpc2j.parser.ast.visitor;
