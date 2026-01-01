/**
 * Pratt parser building blocks for LPC expressions.
 *
 * <p>Defines prefix and infix parselet interfaces alongside concrete implementations for operators,
 * literals, and grouping. Each parselet understands its precedence/associativity and produces the
 * appropriate {@link io.github.protasm.lpc2j.parser.ast.expr} nodes.</p>
 *
 * <p>Assumes the owning {@link io.github.protasm.lpc2j.parser.PrattParser} controls token traversal
 * and assignment context; parselets themselves remain stateless and focused on node construction.</p>
 *
 * <p>Does not perform type analysis—that is deferred to visitors under {@code parser.ast.visitor}.</p>
 */
package io.github.protasm.lpc2j.parser.parselet;
