/**
 * Expression-level AST nodes.
 *
 * <p>Represents literals, variable access, calls, unary and binary operations, and other LPC
 * expression forms. Nodes expose inferred {@link io.github.protasm.lpc2j.parser.type.LPCType} values
 * and support visitor-based compilation and analysis.</p>
 *
 * <p>Assumes surrounding statement or object nodes manage scope and symbol resolution; expression
 * classes themselves are largely immutable apart from inferred type fields.</p>
 *
 * <p>No evaluation happens here—runtime behavior is handled by emitted bytecode and efun
 * implementations.</p>
 */
package io.github.protasm.lpc2j.parser.ast.expr;
