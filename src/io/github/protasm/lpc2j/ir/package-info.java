/**
 * Typed intermediate representation produced after semantic analysis.
 *
 * <p>The IR models runtime-level types and control flow explicitly, providing a stable bridge
 * between parsed ASTs and backend code generation. Nodes are immutable records capturing source
 * line numbers, runtime types, and JVM-oriented call details so later passes can reason about
 * coercions, truthiness, and dispatch without depending on parser internals.</p>
 */
package io.github.protasm.lpc2j.ir;
