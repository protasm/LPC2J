/**
 * Abstract syntax tree structures representing LPC objects.
 *
 * <p>Contains node definitions for objects, fields, methods, parameters, and supporting symbols. Each
 * node participates in multiple visitor passes (printing, type inference, compilation) and retains
 * minimal decoration such as source line numbers and inferred {@link
 * io.github.protasm.lpc2j.parser.type.LPCType} values.</p>
 *
 * <p>Invariants include stable ownership metadata (e.g., object and method names) and visitor support
 * via {@link io.github.protasm.lpc2j.compiler.Compiler} and {@link
 * io.github.protasm.lpc2j.parser.ast.visitor.TypeInferenceVisitor}.</p>
 *
 * <p>This package focuses on structure rather than parsing mechanics (see {@code parser}) or
 * expression/statement specializations (see subpackages).</p>
 */
package io.github.protasm.lpc2j.parser.ast;
