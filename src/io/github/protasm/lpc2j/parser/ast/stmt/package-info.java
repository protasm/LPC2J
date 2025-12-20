/**
 * Statement-level AST nodes.
 *
 * <p>Encapsulates control flow and sequencing constructs such as blocks, returns, conditionals, and
 * expression statements. Statement nodes mainly coordinate child expressions and maintain source
 * lines for diagnostics and code generation.</p>
 *
 * <p>Assumes expression typing and symbol resolution are handled during visitor passes; statements do
 * not independently manage scope beyond their contained locals.</p>
 */
package io.github.protasm.lpc2j.parser.ast.stmt;
