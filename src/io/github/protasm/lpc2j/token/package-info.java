/**
 * Token model classes representing lexical units recognized by the scanner.
 *
 * <p>Provides immutable {@link io.github.protasm.lpc2j.token.Token} records, a strongly typed
 * {@link io.github.protasm.lpc2j.token.TokenType} enumeration that records expected literal classes,
 * and traversal helpers such as {@link io.github.protasm.lpc2j.token.TokenList} for parser
 * consumption.</p>
 *
 * <p>Invariants include matching {@code TokenType.clazz()} to any attached literal and preserving scan
 * order with an explicit EOF sentinel.</p>
 *
 * <p>This package does not perform lexing itself; {@code scanner} is responsible for producing
 * instances.</p>
 */
package io.github.protasm.lpc2j.token;
