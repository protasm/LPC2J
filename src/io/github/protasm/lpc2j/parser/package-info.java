/**
 * Core parser for LPC, producing typed AST structures from token streams.
 *
 * <p>Implements a multi-pass parse ({@code declarations}, {@code definitions}, type inference) driven
 * by a Pratt-style expression parser. The {@link io.github.protasm.lpc2j.parser.Parser} coordinates
 * symbol registration, local scope tracking, and visitor-based post-processing.</p>
 *
 * <p>Assumes tokens have already been validated lexically; raises {@link
 * io.github.protasm.lpc2j.parser.ParseException} when encountering structural issues. Parser behavior
 * is influenced by {@link io.github.protasm.lpc2j.parser.ParserOptions} but leaves semantic
 * enforcement to later stages.</p>
 *
 * <p>This package defines parsing mechanics rather than AST shape (see {@code parser.ast}) or operator
 * definitions (see {@code parser.parselet} and {@code parser.type}).</p>
 */
package io.github.protasm.lpc2j.parser;
